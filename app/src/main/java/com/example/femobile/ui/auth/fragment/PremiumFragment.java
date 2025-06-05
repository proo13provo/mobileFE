package com.example.femobile.ui.auth.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.femobile.R;

public class PremiumFragment extends Fragment {

    private Button btnUpgradePremium;
    private Button btnFreeTrial;

    public PremiumFragment() {
        // Required empty public constructor
    }

    public static PremiumFragment newInstance() {
        return new PremiumFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_premium, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupClickListeners();
    }

    private void initViews(View view) {
        btnUpgradePremium = view.findViewById(R.id.btnUpgradePremium);
        btnFreeTrial = view.findViewById(R.id.btnFreeTrial);
    }

    private void setupClickListeners() {
        btnUpgradePremium.setOnClickListener(v -> {
            // Handle premium upgrade
            showPremiumUpgradeDialog();
        });

        btnFreeTrial.setOnClickListener(v -> {
            // Handle free trial
            startFreeTrial();
        });

        // Set up click listeners for pricing plans
        setupPricingPlanClickListeners();
    }

    private void setupPricingPlanClickListeners() {
        View rootView = getView();
        if (rootView == null) return;

        // Individual Plan
        rootView.findViewById(R.id.cardIndividualPlan).setOnClickListener(v -> {
            selectPremiumPlan("individual", "59.000đ/tháng");
        });

        // Family Plan
        rootView.findViewById(R.id.cardFamilyPlan).setOnClickListener(v -> {
            selectPremiumPlan("family", "79.000đ/tháng");
        });

        // Student Plan
        rootView.findViewById(R.id.cardStudentPlan).setOnClickListener(v -> {
            selectPremiumPlan("student", "29.500đ/tháng");
        });
    }

    private void selectPremiumPlan(String planType, String price) {
        // Show plan selection dialog or navigate to payment
        Toast.makeText(getContext(),
                "Đã chọn gói " + planType + " - " + price,
                Toast.LENGTH_SHORT).show();

        // Here you would typically navigate to payment screen
        // or show a detailed plan selection dialog
        showPaymentOptions(planType, price);
    }

    private void showPremiumUpgradeDialog() {
        // Create and show upgrade dialog
        if (getContext() != null) {
            androidx.appcompat.app.AlertDialog.Builder builder =
                    new androidx.appcompat.app.AlertDialog.Builder(getContext());

            builder.setTitle("Nâng cấp Premium")
                    .setMessage("Bạn có muốn nâng cấp lên Spotify Premium để trải nghiệm âm nhạc không giới hạn?")
                    .setPositiveButton("Nâng cấp", (dialog, which) -> {
                        // Handle upgrade action
                        startPremiumUpgrade();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    private void startFreeTrial() {
        // Handle free trial logic
        Toast.makeText(getContext(),
                "Bắt đầu dùng thử miễn phí 1 tháng!",
                Toast.LENGTH_LONG).show();

        // Here you would typically:
        // 1. Check if user is eligible for free trial
        // 2. Navigate to account creation/payment method setup
        // 3. Activate trial period

        showFreeTrialConfirmation();
    }

    private void showFreeTrialConfirmation() {
        if (getContext() != null) {
            androidx.appcompat.app.AlertDialog.Builder builder =
                    new androidx.appcompat.app.AlertDialog.Builder(getContext());

            builder.setTitle("Dùng thử miễn phí")
                    .setMessage("Bạn sẽ được sử dụng Premium miễn phí trong 1 tháng. Sau đó sẽ tự động gia hạn với giá 59.000đ/tháng. Bạn có thể hủy bất kỳ lúc nào.")
                    .setPositiveButton("Đồng ý", (dialog, which) -> {
                        // Activate free trial
                        activateFreeTrial();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    private void activateFreeTrial() {
        // Here you would make API call to activate free trial
        Toast.makeText(getContext(),
                "Chúc mừng! Bạn đã kích hoạt thành công gói dùng thử Premium!",
                Toast.LENGTH_LONG).show();

        // Update UI to reflect premium status
        // You might want to refresh the main activity or update user preferences
    }

    private void startPremiumUpgrade() {
        // Navigate to premium purchase flow
        showPaymentOptions("individual", "59.000đ/tháng");
    }

    private void showPaymentOptions(String planType, String price) {
        if (getContext() != null) {
            androidx.appcompat.app.AlertDialog.Builder builder =
                    new androidx.appcompat.app.AlertDialog.Builder(getContext());

            String[] paymentMethods = {
                    "Thẻ tín dụng/ghi nợ",
                    "Ví điện tử (MoMo, ZaloPay)",
                    "Thẻ cào điện thoại",
                    "Chuyển khoản ngân hàng"
            };

            builder.setTitle("Chọn phương thức thanh toán")
                    .setItems(paymentMethods, (dialog, which) -> {
                        String selectedMethod = paymentMethods[which];
                        processPayment(planType, price, selectedMethod);
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    private void processPayment(String planType, String price, String paymentMethod) {
        // Handle payment processing
        Toast.makeText(getContext(),
                "Đang xử lý thanh toán " + planType + " qua " + paymentMethod,
                Toast.LENGTH_SHORT).show();

        // Here you would typically:
        // 1. Integrate with payment gateway (Stripe, PayPal, etc.)
        // 2. Process the payment
        // 3. Update user's premium status
        // 4. Show success/failure message

        // For demo purposes, simulate successful payment after 2 seconds
        if (getView() != null) {
            getView().postDelayed(() -> {
                Toast.makeText(getContext(),
                        "Thanh toán thành công! Chào mừng bạn đến với Premium!",
                        Toast.LENGTH_LONG).show();
                
                // Chuyển hướng về Home sau khi thanh toán thành công
                if (getActivity() != null) {
                    // Lưu trạng thái premium vào SharedPreferences
                    android.content.SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
                    prefs.edit().putBoolean("isPremium", true).apply();
                    
                    // Chuyển hướng về Home
                    Intent intent = new Intent(getActivity(), com.example.femobile.ui.auth.MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }
            }, 2000);
        }
    }

    private void openExternalLink(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(),
                    "Không thể mở liên kết",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check if user's premium status has changed
        // and update UI accordingly
    }
}