# Flutter SDK Roadmap: Unified Mobile & Device Verification

> **Document Version:** 1.0.0  
> **Target Platform:** Flutter (Android & iOS)  
> **Domain:** Commercial Banking, Fintech, and High-Assurance Mobile Authentication  
> **Status:** Planned / Architecture Specification  

---

## 1. Executive Summary

This roadmap outlines the design and staged evolution of the **Reverse OTP Flutter SDK**. 

The SDK provides client applications (such as commercial banking apps in Uganda and East Africa) with a frictionless, carrier-grade mobile verification flow. It combines **Carrier-Grade SIM Binding** (via Mobile-Originated SMS) with **Hardware Device Attestation**, adhering to central bank electronic banking and anti-SIM-swap guidelines without requiring restricted carrier or OS entitlements.

---

## 2. Architectural Pillars

```mermaid
graph TD
    subgraph MobileDevice [Flutter Client App]
        UI[Banking Onboarding / Login UI]
        SDK[Reverse OTP Flutter SDK]
        Enclave[Secure Enclave / Android KeyStore]
        NativeSMS[Native SMS Composer / Intent]
    end

    subgraph ServiceInfrastructure [Reverse OTP & Telco Infrastructure]
        TelcoSMSC[Telco SMS Gateway / SMSC<br/>MTN / Airtel Uganda]
        ReverseOTPService[Reverse OTP Cloud Run Service]
        BankBackend[Core Banking / Auth Backend]
    end

    UI -->|1. Start Verification| SDK
    SDK -->|2. Generate Key Pair| Enclave
    SDK -->|3. POST /tokens/generate| ReverseOTPService
    SDK -->|4. Launch Pre-filled SMS| NativeSMS
    NativeSMS -->|5. Outbound MO SMS| TelcoSMSC
    TelcoSMSC -->|6. MO Webhook with MSISDN| ReverseOTPService
    SDK -->|7. Hand over requestId| BankBackend
    BankBackend -->|8. GET /tokens/{requestId}/status| ReverseOTPService
    ReverseOTPService -->>|9. Status: APPROVED| BankBackend
    BankBackend -->|10. Bind Account to Device| UI
```

1. **Carrier-Grade SIM Binding (Zero-Tampering):**  
   The phone number is verified at the cellular baseband / tower level. The calling line identity (`MSISDN`) cannot be spoofed by a rooted or jailbroken device.
2. **Server-to-Server Trust (Zero-Client-Spoofing):**  
   The Flutter SDK never authorizes a user directly. It returns a cryptographically secure `requestId` to the host app's backend, which validates the status with the Reverse OTP service directly.
3. **Zero Restricted Entitlements (100% App Store & Play Store Compliant):**  
   Works globally without requiring region-locked capabilities (such as India's NPCI `upi-device-validation` entitlement).

---

## 3. Platform Capabilities & Nuances

### 3.1 Android Strategy

| Feature | Implementation | Permissions | Friction |
| :--- | :--- | :--- | :--- |
| **Phone Number Selection** | Google Play Services **Phone Number Hint API** | None | 1 tap (System sheet prompts user to pick active SIM) |
| **SMS Dispatch (Default)** | System SMS Intent (`sms:+94...?body=...`) | None | 1 tap (Pre-fills recipient & token; user taps send) |
| **SMS Dispatch (Silent)** | Native `SmsManager.sendTextMessage()` | `SEND_SMS` | 0 taps (Subject to Google Play sensitive permission review) |
| **Dual-SIM Support** | `SubscriptionManager` slot detection | `READ_PHONE_STATE` (Optional) | Advises user if sending SIM mismatches registered SIM |

### 3.2 iOS Strategy

| Feature | Implementation | Permissions | Notes |
| :--- | :--- | :--- | :--- |
| **Phone Number Autofill** | `UITextContentType.telephoneNumber` | None | Suggests number from user's Contact Card |
| **SMS Dispatch** | `MFMessageComposeViewController` (`MessageUI`) | None | Native iOS modal sheet with non-editable pre-filled fields |
| **Hardware Attestation** | Apple `DCAppAttestService` (DeviceCheck) | None | Cryptographically attests device authenticity in Secure Enclave |
| **CTCellularPlanStatus** | *Not applicable in Uganda* | `upi-device-validation` | Restricted exclusively to India/NPCI payment apps |

---

## 4. Multi-Phase Roadmap

### Phase 1: Core Client & SMS Dispatch (MVP)
* **Goal:** Abstract all HTTP communications and OS-level SMS launch mechanics.
* **Features:**
  - Automated `POST /api/v1/tokens/generate` integration.
  - Native SMS launch on Android (via standard intent) and iOS (via `MFMessageComposeViewController`).
  - Automatic injection of `recipientNumber` and plain hex `smsContent`.
  - Return `requestId` and expiration metadata to the host app.
  - Cancellation & timeout callbacks.

### Phase 2: Frictionless Device Pre-Checks
* **Goal:** Eliminate manual phone number typing.
* **Features:**
  - **Android:** Integrate Google Play Services Identity Credential Manager / Phone Number Hint API.
  - **iOS:** Optimized QuickType text input decoration.
  - E.164 normalization engine (`PhoneNumberUtil` matching backend logic).
  - Dual-SIM detection & UI guidance tooltips.

### Phase 3: Bank-Grade Device Binding & Hardware Attestation
* **Goal:** Comply with Bank of Uganda / Central Bank electronic banking standards.
* **Features:**
  - **iOS:** Generate key pair via Apple's Secure Enclave using `DCAppAttestService`.
  - **Android:** Hardware-backed key attestation via Android KeyStore / Play Integrity API.
  - Transmit the public key attestation along with the verification request for backend device registration.

### Phase 4: Omnichannel Multi-Rail Waterfall (Future Expansion)
* **Goal:** Seamless fallback across multiple verification technologies.
* **Waterfall Logic:**
  1. **Rail 1 (SNA - Silent Network Auth):** 0-tap carrier data verification over cellular APN (via GSMA Open Gateway APIs with MTN/Airtel if mobile data is active).
  2. **Rail 2 (Reverse OTP):** 1-tap MO SMS (our core engine, optimal for Wi-Fi and 2G/3G/4G/5G).
  3. **Rail 3 (Traditional OTP + Auto-Read):** Downstream fallback with SMS Retriever API (Android) / SMS Autofill (iOS).

---

## 5. Draft SDK API Specification (Dart)

```dart
import 'package:reverse_otp/reverse_otp.dart';

void main() {
  // 1. Global Initialization (in main.dart or bootstrap)
  ReverseOtp.initialize(
    config: ReverseOtpConfig(
      baseUrl: 'https://reverse-otp-service-865728060831.asia-southeast1.run.app',
      timeout: Duration(seconds: 30),
    ),
  );
}

// 2. Client Flow inside a Banking Registration Screen
Future<void> initiateBankVerification(BuildContext context) async {
  try {
    // Optional: Request phone number hint on Android (1-tap UI sheet)
    final suggestedNumber = await ReverseOtp.requestPhoneNumberHint();

    // Launch Reverse OTP Flow
    final result = await ReverseOtp.startVerification(
      phoneNumber: suggestedNumber ?? '+256771234567',
      onSmsSheetOpened: () {
        // Show in-app waiting spinner
      },
    );

    // Hand over verified requestId to host banking backend
    await myBankBackend.confirmDeviceRegistration(
      requestId: result.requestId,
      phoneNumber: result.phoneNumber,
      expiresAt: result.expiresAt,
    );

  } on ReverseOtpUserCancelledException {
    // User closed the SMS composer without sending
  } on ReverseOtpNetworkException catch (e) {
    // Network / service error
  }
}
```

---

## 6. Regulatory & Security Checklist (Uganda / Central Bank)

- [x] **No Hardcoded Secrets on Client:** Reverse OTP service does not expose credentials to the mobile app.
- [x] **Server-to-Server Validation:** Host app backend verifies the token status via authenticated endpoint before account activation.
- [x] **Protection Against SIM Swap:** Because the SMS must originate from the SIM card at that exact instant, historical or forwarded credentials cannot be reused.
- [x] **Immutable Audit Trail:** Incoming message ID, timestamp, and carrier MSISDN are persisted in `incoming_messages` and linked to `token_requests`.
