import SwiftUI
import ComposeApp
// [firebase] start
// import FirebaseCore
// import FirebaseMessaging
// [firebase] end

class AppDelegate: NSObject, UIApplicationDelegate {
    // [firebase] start
    // Note: Add MessagingDelegate, UNUserNotificationCenterDelegate to AppDelegate conformance
    // [firebase] end

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // [firebase] start
        // FirebaseApp.configure()
        // Messaging.messaging().delegate = self
        // UNUserNotificationCenter.current().delegate = self
        // [firebase] end

        // [firestore] FirestoreServiceFactory.registerFactory()

        // [push_notifications] start
        // application.registerForRemoteNotifications()
        // [push_notifications] end

        return true
    }

    // [deep_linking] start
    // func application(
    //     _ app: UIApplication,
    //     open url: URL,
    //     options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    // ) -> Bool {
    //     DeepLinkBridgeKt.handleDeepLinkUri(uri: url.absoluteString)
    //     return true
    // }
    // [deep_linking] end

    // [push_notifications] start
    // func application(
    //     _ application: UIApplication,
    //     didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    // ) {
    //     // [firebase] start
    //     // Messaging.messaging().apnsToken = deviceToken
    //     // [firebase] end
    // }
    // //
    // // [firebase] start
    // // func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
    // //     guard let token = fcmToken else { return }
    // //     IosPushNotificationService.companion.fcmToken = token
    // // }
    // //
    // // func userNotificationCenter(
    // //     _ center: UNUserNotificationCenter,
    // //     willPresent notification: UNNotification,
    // //     withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    // // ) {
    // //     let userInfo = notification.request.content.userInfo
    // //     let messageId = userInfo["gcm.message_id"] as? String ?? ""
    // //     let title = notification.request.content.title
    // //     let body = notification.request.content.body
    // //
    // //     IosPushNotificationService.companion.onMessageReceived(
    // //         messageId: messageId,
    // //         title: title,
    // //         body: body,
    // //         data: [:]
    // //     )
    // //
    // //     completionHandler([.banner, .sound, .badge])
    // // }
    // // [firebase] end
    // [push_notifications] end
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
