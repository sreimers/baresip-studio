#include <string.h>
#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <re.h>
#include <baresip.h>

#define LOGD(...) \
  ((void)__android_log_print(ANDROID_LOG_DEBUG, "Baresip", __VA_ARGS__))

#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, "Baresip", __VA_ARGS__))

#define LOGW(...) \
  ((void)__android_log_print(ANDROID_LOG_WARN, "Baresip", __VA_ARGS__))

#define LOGE(...) \
  ((void)__android_log_print(ANDROID_LOG_ERROR, "Baresip", __VA_ARGS__))

static void signal_handler(int sig)
{
	static bool term = false;

	if (term) {
		mod_close();
		exit(0);
	}

	term = true;
    LOGI("terminated by signal (%d)\n", sig);
    ua_stop_all(false);
}

static void ua_exit_handler(void *arg)
{
	(void)arg;
	LOGD("ua exited -- stopping main runloop\n");
	re_cancel();
}

static void ua_event_handler(struct ua *ua, enum ua_event ev,
			     struct call *call, const char *prm, void *arg)
{
	LOGD("ua event (%s)\n", uag_event_str(ev));
}

#include <pthread.h>
#include <unistd.h>

static int pfd[2];
static pthread_t loggingThread;

static void *loggingFunction() {
    ssize_t readSize;
    char buf[128];

    while((readSize = read(pfd[0], buf, sizeof buf - 1)) > 0) {
        if(buf[readSize - 1] == '\n') {
            --readSize;
        }
        buf[readSize] = 0;
        LOGD("%s", buf);
    }

    return 0;
}

static int runLoggingThread() {
    setvbuf(stdout, 0, _IOLBF, 0);
    setvbuf(stderr, 0, _IONBF, 0);

    pipe(pfd);
    dup2(pfd[1], 1);
    dup2(pfd[1], 2);

    if (pthread_create(&loggingThread, 0, loggingFunction, 0) == -1) {
        return -1;
    }

    pthread_detach(loggingThread);

    return 0;
}

JNIEXPORT void JNICALL
Java_com_tutpro_baresip_MainActivity_baresipStart(JNIEnv *env, jobject thiz)
{
    int err;
    char *path = "/data/data/com.tutpro.baresip/files";

    runLoggingThread();

    err = libre_init();
    if (err)
	    goto out;

    conf_path_set(path);
    
    err = conf_configure();
    if (err) {
        LOGW("conf_configure() failed: (%d)\n", err);
        goto out;
    }

    err = baresip_init(conf_config(), false);
    if (err) {
        LOGW("baresip_init() failed (%d)\n", err);
        goto out;
    }

    play_set_path(baresip_player(), path);

    err = ua_init("baresip v" BARESIP_VERSION " (" ARCH "/" OS ")",
                  true, true, true, false);
    if (err) {
        LOGW("ua_init() failed (%d)\n", err);
        goto out;
    }

    uag_set_exit_handler(ua_exit_handler, NULL);
    uag_event_register(ua_event_handler, NULL);

    err = conf_modules();
    if (err) {
        LOGW("conf_modules() failed (%d)\n", err);
        goto out;
    }

    LOGI("Running main loop\n");
    err = re_main(signal_handler);

out:

    if (err) {
        LOGE("error: (%d)\n", err);
    }

    LOGD("closing");
    conf_close();
    baresip_close();
    mod_close();
    /* libre_close(); */

    tmr_debug();
    mem_debug();

    return;
}

JNIEXPORT void JNICALL
Java_com_tutpro_baresip_MainActivity_baresipStop(JNIEnv *env, jobject thiz)
{
    LOGD("Stop all ua ...\n");
    ua_stop_all(false);
    LOGD("Close ua ...\n");
    ua_close();

    return;
}
