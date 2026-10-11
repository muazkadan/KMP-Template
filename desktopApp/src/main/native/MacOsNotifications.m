// JNI side of dev.muazkadan.myapplication.data.notifications.MacOsNotificationBridge: posts
// notifications through UserNotifications.framework, so macOS shows them under the app's own name
// and icon, and reports clicks back to the JVM. Built by desktopApp's compileMacOsNotifications task.

#import <jni.h>
#import <Cocoa/Cocoa.h>
#import <UserNotifications/UserNotifications.h>

static JavaVM *g_jvm = NULL;
static jclass g_bridgeClass = NULL;
static jmethodID g_clickCallback = NULL;

static void notifyJvmOfClick(void) {
    if (g_jvm == NULL || g_bridgeClass == NULL || g_clickCallback == NULL) return;

    JNIEnv *env = NULL;
    BOOL attached = NO;
    jint status = (*g_jvm)->GetEnv(g_jvm, (void **)&env, JNI_VERSION_1_8);
    if (status == JNI_EDETACHED) {
        if ((*g_jvm)->AttachCurrentThread(g_jvm, (void **)&env, NULL) != JNI_OK) return;
        attached = YES;
    } else if (status != JNI_OK) {
        return;
    }

    (*env)->CallStaticVoidMethod(env, g_bridgeClass, g_clickCallback);
    // An exception left pending on a thread the JVM does not own would abort it
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
    }

    if (attached) {
        (*g_jvm)->DetachCurrentThread(g_jvm);
    }
}

@interface MacOsNotificationDelegate : NSObject <UNUserNotificationCenterDelegate>
@end

@implementation MacOsNotificationDelegate

// Without this macOS drops notifications while the app is in front
- (void)userNotificationCenter:(UNUserNotificationCenter *)center
       willPresentNotification:(UNNotification *)notification
         withCompletionHandler:(void (^)(UNNotificationPresentationOptions options))completionHandler {
    completionHandler(UNNotificationPresentationOptionBanner | UNNotificationPresentationOptionList |
                      UNNotificationPresentationOptionSound);
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center
didReceiveNotificationResponse:(UNNotificationResponse *)response
         withCompletionHandler:(void (^)(void))completionHandler {
    if ([response.actionIdentifier isEqualToString:UNNotificationDefaultActionIdentifier]) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if (@available(macOS 14.0, *)) {
                [NSApp activate];
            } else {
                [NSApp activateIgnoringOtherApps:YES];
            }
        });
        notifyJvmOfClick();
    }
    completionHandler();
}

@end

static MacOsNotificationDelegate *g_delegate = nil;

// UNUserNotificationCenter needs a real .app bundle, and raises an exception that takes the whole
// process down otherwise. A bundle identifier alone is not enough: the JDK's java launcher embeds
// an Info.plist with one, so `./gradlew run` reports e.g. com.jetbrains.jbr.java.
static BOOL isRunningFromAppBundle(void) {
    NSBundle *bundle = NSBundle.mainBundle;
    return bundle.bundleIdentifier.length > 0 &&
           [bundle.bundleURL.pathExtension caseInsensitiveCompare:@"app"] == NSOrderedSame;
}

static NSString *stringFromJava(JNIEnv *env, jstring value) {
    if (value == NULL) return @"";
    const jchar *chars = (*env)->GetStringChars(env, value, NULL);
    if (chars == NULL) return @"";
    NSString *result = [NSString stringWithCharacters:chars length:(NSUInteger)(*env)->GetStringLength(env, value)];
    (*env)->ReleaseStringChars(env, value, chars);
    return result;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_8) != JNI_OK) return JNI_ERR;

    jclass bridgeClass = (*env)->FindClass(env, "dev/muazkadan/myapplication/data/notifications/MacOsNotificationBridge");
    if (bridgeClass == NULL) return JNI_ERR;
    jmethodID callback = (*env)->GetStaticMethodID(env, bridgeClass, "onNotificationClicked", "()V");
    if (callback == NULL) return JNI_ERR;

    g_jvm = vm;
    g_bridgeClass = (jclass)(*env)->NewGlobalRef(env, bridgeClass);
    g_clickCallback = callback;
    (*env)->DeleteLocalRef(env, bridgeClass);
    return JNI_VERSION_1_8;
}

JNIEXPORT jboolean JNICALL Java_dev_muazkadan_myapplication_data_notifications_MacOsNotificationBridge_nativeInit(JNIEnv *env, jclass cls) {
    if (!isRunningFromAppBundle()) return JNI_FALSE;

    static dispatch_once_t once;
    dispatch_once(&once, ^{
        g_delegate = [[MacOsNotificationDelegate alloc] init];
        UNUserNotificationCenter *center = UNUserNotificationCenter.currentNotificationCenter;
        center.delegate = g_delegate;
        [center requestAuthorizationWithOptions:(UNAuthorizationOptionAlert | UNAuthorizationOptionSound)
                              completionHandler:^(BOOL granted, NSError *_Nullable error) {
                                  if (error != nil) {
                                      NSLog(@"[MacOsNotifications] Authorization failed: %@", error);
                                  }
                              }];
    });
    return JNI_TRUE;
}

JNIEXPORT void JNICALL Java_dev_muazkadan_myapplication_data_notifications_MacOsNotificationBridge_nativePostNotification(
    JNIEnv *env,
    jclass cls,
    jstring jTitle,
    jstring jBody,
    jboolean withSound
) {
    @autoreleasepool {
        UNMutableNotificationContent *content = [[UNMutableNotificationContent alloc] init];
        content.title = stringFromJava(env, jTitle);
        content.body = stringFromJava(env, jBody);
        if (withSound) {
            content.sound = UNNotificationSound.defaultSound;
        }

        // A nil trigger delivers right away
        UNNotificationRequest *request = [UNNotificationRequest requestWithIdentifier:NSUUID.UUID.UUIDString
                                                                              content:content
                                                                              trigger:nil];
        [UNUserNotificationCenter.currentNotificationCenter addNotificationRequest:request
                                                             withCompletionHandler:^(NSError *_Nullable error) {
                                                                 if (error != nil) {
                                                                     NSLog(@"[MacOsNotifications] Not delivered: %@", error);
                                                                 }
                                                             }];
    }
}
