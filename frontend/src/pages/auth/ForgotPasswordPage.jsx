import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Link, useNavigate } from "react-router-dom";

import { forgotPassword } from "@/api/auth";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { saveResetDebugOtp, saveResetPasswordEmail } from "@/features/auth/resetPasswordSession";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ROUTES } from "@/routes/paths";

export function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");

  const forgotPasswordMutation = useMutation({
    mutationFn: forgotPassword,
    onSuccess: (data) => {
      saveResetPasswordEmail(data.email || email);
      saveResetDebugOtp(data.debugOtp || "");
      navigate(ROUTES.verifyResetOtp, {
        replace: true
      });
    }
  });

  const handleSubmit = (event) => {
    event.preventDefault();
    forgotPasswordMutation.mutate({ email });
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Forgot password</CardTitle>
        <CardDescription>
          Request a reset OTP for your account. After submitting, we will take you straight to the code verification step.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form className="space-y-4" onSubmit={handleSubmit}>
          {forgotPasswordMutation.isError && (
            <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {forgotPasswordMutation.error.message}
            </div>
          )}
          <div className="space-y-2">
            <Label htmlFor="forgot-email">Email</Label>
            <Input
              id="forgot-email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="you@example.com"
              required
            />
          </div>
          <Button className="w-full" type="submit">
            {forgotPasswordMutation.isPending ? "Sending reset OTP..." : "Send reset OTP"}
          </Button>
          <p className="text-center text-sm text-muted-foreground">
            Remembered your password? <Link className="text-primary hover:underline" to={ROUTES.login}>Back to login</Link>
          </p>
        </form>
      </CardContent>
    </Card>
  );
}
