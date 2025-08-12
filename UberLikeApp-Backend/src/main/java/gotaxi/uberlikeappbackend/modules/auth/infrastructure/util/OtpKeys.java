package gotaxi.uberlikeappbackend.modules.auth.infrastructure.util;

public final class OtpKeys {

    private OtpKeys() {}

    public static String sendId(String identifier) {
        return "otp:send:id:" + identifier;
    }

    public static String vId(String identifier) {
        return "otp:verify:id:" + identifier;
    }

    public static String vPreDev(String deviceId) {
        return "otp:verify:pre:dev:" + deviceId;
    }

    public static String loginId(String identifier) {
        return "login:fail:id:" + identifier;
    }

    public static String loginDevice(String deviceId) {
        return "login:fail:device:" + deviceId;
    }

}