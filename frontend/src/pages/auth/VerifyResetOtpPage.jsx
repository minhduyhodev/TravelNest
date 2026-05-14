import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Link, useNavigate } from "react-router-dom";

import { verifyResetOtp } from "@/api/auth";
import {
  getResetDebugOtp,
  getResetPasswordEmail,
  saveResetPasswordSession
} from "@/features/auth/resetPasswordSession";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ROUTES } from "@/routes/paths";

export function VerifyResetOtpPage() {
  const navigate = useNavigate();
  const resetEmail = getResetPasswordEmail();
  const debugOtp = getResetDebugOtp();
  const [otp, setOtp] = useState(debugOtp);

  const verifyResetOtpMutation = useMutation({
    mutationFn: verifyResetOtp,
    onSuccess: (data) => {
      saveResetPasswordSession(data.email, data.resetToken);
      navigate(ROUTES.resetPassword, { replace: true });
    }
  });

  const handleSubmit = (event) => {
    event.preventDefault();
    if (!resetEmail) {
      return;
    }

    verifyResetOtpMutation.mutate({ email: resetEmail, otp });
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Verify reset code</CardTitle>
        <CardDescription>
          Enter the OTP from your email to continue to the password reset step.
        </CardDescription>
      </CardHeader>
      <CardContent>
        {!resetEmail ? (
          <div className="space-y-4">
            <div className="rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
              Missing reset email context. Start again from forgot password.
            </div>
            <Button asChild className="w-full">
              <Link to={ROUTES.forgotPassword}>Back to forgot password</Link>
            </Button>
          </div>
        ) : (
        <form className="space-y-4" onSubmit={handleSubmit}>
          {debugOtp && (
            <div className="rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
              Development OTP: <span className="font-semibold tracking-[0.2em]">{debugOtp}</span>
            </div>
          )}
          {verifyResetOtpMutation.isError && (
            <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {verifyResetOtpMutation.error.message}
            </div>
          )}
          <div className="space-y-2">
            <Label htmlFor="verify-otp">OTP</Label>
            <Input
              id="verify-otp"
              value={otp}
              onChange={(event) => setOtp(event.target.value)}
              placeholder="6-digit OTP"
              required
            />
          </div>
          <Button className="w-full" type="submit">
            {verifyResetOtpMutation.isPending ? "Verifying..." : "Verify code"}
          </Button>
          <p className="text-center text-sm text-muted-foreground">
            Need a new OTP? <Link className="text-primary hover:underline" to={ROUTES.forgotPassword}>Request again</Link>
          </p>
        </form>
        )}
      </CardContent>
    </Card>
  );
}
