import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Link, useNavigate } from "react-router-dom";

import { resetPassword } from "@/api/auth";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import {
  clearResetPasswordSession,
  getResetPasswordEmail,
  getResetPasswordSession
} from "@/features/auth/resetPasswordSession";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ROUTES } from "@/routes/paths";

export function ResetPasswordPage() {
  const navigate = useNavigate();
  const resetEmail = getResetPasswordEmail();
  const [form, setForm] = useState({
    newPassword: "",
    confirmPassword: ""
  });
  const [localError, setLocalError] = useState(() =>
    resetEmail && getResetPasswordSession()
      ? ""
      : "Verify your reset code before choosing a new password."
  );

  const resetPasswordMutation = useMutation({
    mutationFn: resetPassword,
    onSuccess: () => {
      clearResetPasswordSession();
      setTimeout(() => navigate(ROUTES.login, { replace: true }), 1200);
    }
  });

  const handleSubmit = (event) => {
    event.preventDefault();
    setLocalError("");

    if (form.newPassword !== form.confirmPassword) {
      setLocalError("Password confirmation does not match.");
      return;
    }

    const resetToken = getResetPasswordSession();
    if (!resetToken) {
      setLocalError("Your reset verification has expired. Verify the code again.");
      return;
    }

    resetPasswordMutation.mutate({
      email: resetEmail,
      resetToken,
      newPassword: form.newPassword
    });
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Reset password</CardTitle>
        <CardDescription>Choose and confirm a new password after your reset code has been verified.</CardDescription>
      </CardHeader>
      <CardContent>
        {!resetEmail ? (
          <div className="space-y-4">
            <div className="rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
              Missing verified reset session. Start again from forgot password.
            </div>
            <Button asChild className="w-full">
              <Link to={ROUTES.forgotPassword}>Back to forgot password</Link>
            </Button>
          </div>
        ) : (
        <form className="space-y-4" onSubmit={handleSubmit}>
          {(localError || resetPasswordMutation.isError) && (
            <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {localError || resetPasswordMutation.error.message}
            </div>
          )}
          {resetPasswordMutation.isSuccess && (
            <div className="rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
              Password reset successfully. Redirecting to login...
            </div>
          )}
          <div className="space-y-2">
            <Label htmlFor="reset-password">New password</Label>
            <Input
              id="reset-password"
              type="password"
              value={form.newPassword}
              onChange={(event) =>
                setForm((current) => ({ ...current, newPassword: event.target.value }))
              }
              required
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="reset-confirm-password">Confirm new password</Label>
            <Input
              id="reset-confirm-password"
              type="password"
              value={form.confirmPassword}
              onChange={(event) =>
                setForm((current) => ({ ...current, confirmPassword: event.target.value }))
              }
              required
            />
          </div>
          <Button className="w-full" type="submit">
            {resetPasswordMutation.isPending ? "Resetting password..." : "Reset password"}
          </Button>
          <p className="text-center text-sm text-muted-foreground">
            Need to verify again? <Link className="text-primary hover:underline" to={ROUTES.verifyResetOtp}>Back to code verification</Link>
          </p>
        </form>
        )}
      </CardContent>
    </Card>
  );
}
