import Foundation
import ComposeApp

@objc(FirestoreServiceFactory)
public class FirestoreServiceFactory: NSObject {
    @objc public static func registerFactory() {
        IosFirestoreServiceKt.registerFirestoreServiceFactory {
            return IOSFirestoreService.shared
        }
    }
}
