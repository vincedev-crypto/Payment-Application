// payment/src/main/resources/static/js/payment.js
document.addEventListener('DOMContentLoaded', function () {
    // --- Get all the form elements (Your original code) ---
    const billingForm = document.getElementById('billingForm');
    const paymentForm = document.getElementById('paymentForm');
    const billingSection = document.getElementById('billingSection');
    const paymentSection = document.getElementById('paymentSection');
    const backToBillingBtn = document.getElementById('backToBilling');
    const paymentMethodSelect = document.getElementById('paymentMethod');
    const confirmPaymentBtn = document.getElementById('confirmPaymentBtn');

    // --- All your payment method field containers (Your original code) ---
    const gcashFields = document.getElementById('gcashFields');
    const cardFields = document.getElementById('cardFields');
    const bdoFields = document.getElementById('bdoFields');
    const paymayaFields = document.getElementById('paymayaFields');
    
    // --- Your existing modal and loading elements (Your original code) ---
    const confirmModalElement = document.getElementById('confirmModal');
    const confirmModal = confirmModalElement ? new bootstrap.Modal(confirmModalElement) : null;
    const confirmAmountSpan = document.getElementById('confirmAmount');
    const confirmMethodSpan = document.getElementById('confirmMethod');
    const confirmYesBtn = document.getElementById('confirmYes');
    const loadingOverlay = document.getElementById('loadingOverlay');

    // --- FIX: Define all input fields that need conditional validation ---
    const cardFieldsInputs = [
        document.getElementById('cardNumber'),
        document.getElementById('cardName'),
        document.getElementById('expiry'),
        document.getElementById('cvv')
    ];
    const bdoInput = document.getElementById('bdoAccountNumber'); // Now has an ID from HTML fix

    // Helper function to set required/disabled state for an array of fields
    function setFieldsState(inputs, isRequired) {
        if (!Array.isArray(inputs)) {
             // Handle single element case if necessary, or ensure inputs is always an array
             inputs = inputs ? [inputs] : [];
        }
        inputs.forEach(input => {
            if (input) {
                input.required = isRequired;
                input.disabled = !isRequired;
            }
        });
    }

    // --- Step 1: Handle Billing Form Submission (Your original code) ---
    if (billingForm) {
        billingForm.addEventListener('submit', function (event) {
            event.preventDefault();
            event.stopPropagation();
            billingForm.classList.add('was-validated');
            if (billingForm.checkValidity()) {
                // Transfer data from billing form to the hidden payment form fields
                document.getElementById('formName').value = document.getElementById('fullName').value;
                document.getElementById('formEmail').value = document.getElementById('email').value;
                document.getElementById('formPhone').value = document.getElementById('phone').value;
                document.getElementById('formAddress').value = document.getElementById('address').value;
                document.getElementById('formCity').value = document.getElementById('city').value;
                document.getElementById('formCountry').value = document.getElementById('country').value;
                document.getElementById('formPostalCode').value = document.getElementById('postalCode').value;
                document.getElementById('formAmount').value = document.getElementById('amount').value;
                
                // Switch to the payment section
                billingSection.classList.add('d-none');
                paymentSection.classList.remove('d-none');
                
                // Reset required state on payment form fields on section load
                setFieldsState(cardFieldsInputs, false);
                setFieldsState(bdoInput ? [bdoInput] : [], false);
            }
        });
    }

    // --- Step 2: Handle Payment Method Selection (UPDATED) ---
    if (paymentMethodSelect) {
        paymentMethodSelect.addEventListener('change', function () {
            // Hide all payment method fields first
            if (cardFields) cardFields.classList.add('d-none');
            if (gcashFields) gcashFields.classList.add('d-none');
            if (bdoFields) bdoFields.classList.add('d-none');
            if (paymayaFields) paymayaFields.classList.add('d-none');

            // By default, show the main confirm button
            if (confirmPaymentBtn) confirmPaymentBtn.classList.remove('d-none');
            
            const selectedMethod = this.value;

            // --- FIX: RESET/DISABLE ALL CONDITIONALLY REQUIRED FIELDS ---
            setFieldsState(cardFieldsInputs, false);
            setFieldsState(bdoInput ? [bdoInput] : [], false);

            if (selectedMethod === 'gcash') {
                if (gcashFields) gcashFields.classList.remove('d-none');
                if (confirmPaymentBtn) confirmPaymentBtn.classList.add('d-none');
                generateDynamicQRCode('gcash');
            } else if (selectedMethod === 'paymaya') {
                if (paymayaFields) paymayaFields.classList.remove('d-none');
                if (confirmPaymentBtn) confirmPaymentBtn.classList.add('d-none');
                generateDynamicQRCode('paymaya');
            } else if (selectedMethod === 'credit' || selectedMethod === 'debit') {
                if (cardFields) cardFields.classList.remove('d-none');
                // ENABLE AND REQUIRE CARD FIELDS
                setFieldsState(cardFieldsInputs, true);
            } else if (selectedMethod === 'bdo') {
                if (bdoFields) bdoFields.classList.remove('d-none');
                // ENABLE AND REQUIRE BDO FIELD
                setFieldsState(bdoInput ? [bdoInput] : [], true);
            }
        });
    }

    // --- NEW REUSABLE FUNCTION for generating QR Codes (existing code) ---
    function generateDynamicQRCode(method) {
        const amount = document.getElementById('formAmount').value;
        const name = encodeURIComponent(document.getElementById('formName').value);
        const email = encodeURIComponent(document.getElementById('formEmail').value);
        const phone = encodeURIComponent(document.getElementById('formPhone').value);

        let qrImage, qrLoading, qrUrl;

        // Determine which elements and URL to use based on the selected method
        if (method === 'gcash') {
            qrImage = document.getElementById('dynamicGcashQrCode');
            qrLoading = document.getElementById('qrLoadingGcash');
            qrUrl = `/generate-dynamic-qr?amount=${amount}&name=${name}&email=${email}&phone=${phone}`;
        } else if (method === 'paymaya') {
            qrImage = document.getElementById('dynamicPaymayaQrCode');
            qrLoading = document.getElementById('qrLoadingPaymaya');
            qrUrl = `/generate-paymaya-qr?amount=${amount}&name=${name}&email=${email}&phone=${phone}`;
        } else {
            return; 
        }

        if (!qrImage || !qrLoading) {
            console.error("Required QR code elements are missing from the HTML for method: " + method);
            return;
        }

        qrLoading.classList.remove('d-none');
        qrImage.src = '';
        qrImage.classList.add('d-none');

        if (amount && parseFloat(amount) >= 20) {
            qrImage.src = qrUrl;
            qrImage.onload = () => {
                qrLoading.classList.add('d-none');
                qrImage.classList.remove('d-none');
            };
            qrImage.onerror = () => {
                qrLoading.innerHTML = '<p class="text-danger">Failed to load QR Code. Please refresh and try again.</p>';
            };
        } else {
            qrLoading.innerHTML = '<p class="text-danger">Invalid amount. Please go back and enter at least ₱20.00.</p>';
        }
    }

    // --- Handle "Back" button (Your original code) ---
    if (backToBillingBtn) {
        backToBillingBtn.addEventListener('click', function() {
            paymentSection.classList.add('d-none');
            billingSection.classList.remove('d-none');
            if(billingForm) billingForm.classList.remove('was-validated');
        });
    }

    // --- Handle Final Form Submission (Your original code) ---
    if (paymentForm) {
        paymentForm.addEventListener('submit', function(event) {
            const selectedMethod = paymentMethodSelect.value;
            // Prevent submission for all QR methods
            if (selectedMethod === 'gcash' || selectedMethod === 'paymaya') {
                event.preventDefault();
                alert('Please scan the QR code with your payment app to complete the transaction.');
                return;
            }
            
            // Your existing modal logic for other payment methods
            event.preventDefault(); 
            // FIX: Manually trigger form validation before showing modal
            if (!paymentForm.checkValidity()) {
                paymentForm.classList.add('was-validated');
                return; // Stop if form validation fails
            }
            
            const amount = document.getElementById('formAmount').value;
            const method = paymentMethodSelect.options[paymentMethodSelect.selectedIndex].text;
            if (confirmAmountSpan) confirmAmountSpan.textContent = parseFloat(amount).toFixed(2);
            if (confirmMethodSpan) confirmMethodSpan.textContent = method;
            if (confirmModal) confirmModal.show();
        });
    }
    
    // --- Your original modal confirmation logic (No changes here) ---
    if (confirmYesBtn) {
        confirmYesBtn.addEventListener('click', function() {
            if (confirmModal) confirmModal.hide();
            if (loadingOverlay) loadingOverlay.classList.remove('d-none');
            
            // Now, actually submit the form
            if (paymentForm) paymentForm.submit();
        });
    }
});