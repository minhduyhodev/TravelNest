import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";

import { register as registerAccount } from "@/api/auth";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuthStore } from "@/stores/useAuthStore";
import { registerSchema } from "@/utils/validation/registerSchema";

export function RegisterPage() {
  const navigate = useNavigate();
  const setSession = useAuthStore((state) => state.setSession);
  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      fullName: "",
      email: "",
      phone: "",
      preferredLang: "vi",
      password: "",
      confirmPassword: ""
    }
  });

  const registerMutation = useMutation({
    mutationFn: registerAccount,
    onSuccess: (session) => {
      setSession(session);
      navigate("/account", { replace: true });
    }
  });

  const onSubmit = (values) => {
    registerMutation.mutate({
      fullName: values.fullName,
      email: values.email,
      phone: values.phone || null,
      preferredLang: values.preferredLang,
      password: values.password
    });
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Create account</CardTitle>
        <CardDescription>
          Customer registration wired to the Spring Boot auth API, ready for Phase 1 expansion.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
          {registerMutation.isError && (
            <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {registerMutation.error.message}
            </div>
          )}
          <div className="space-y-2">
            <Label htmlFor="fullName">Full name</Label>
            <Input id="fullName" {...register("fullName")} placeholder="Nguyen Van A" />
            {errors.fullName && (
              <p className="text-sm text-status-danger">{errors.fullName.message}</p>
            )}
          </div>
          <div className="space-y-2">
            <Label htmlFor="email">Email</Label>
            <Input id="email" {...register("email")} placeholder="you@example.com" />
            {errors.email && <p className="text-sm text-status-danger">{errors.email.message}</p>}
          </div>
          <div className="space-y-2">
            <Label htmlFor="phone">Phone</Label>
            <Input id="phone" {...register("phone")} placeholder="0901234567" />
            {errors.phone && <p className="text-sm text-status-danger">{errors.phone.message}</p>}
          </div>
          <div className="space-y-2">
            <Label htmlFor="preferredLang">Preferred language</Label>
            <select
              id="preferredLang"
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              {...register("preferredLang")}
            >
              <option value="vi">Vietnamese</option>
              <option value="en">English</option>
            </select>
          </div>
          <div className="space-y-2">
            <Label htmlFor="password">Password</Label>
            <Input id="password" type="password" {...register("password")} placeholder="........" />
            {errors.password && (
              <p className="text-sm text-status-danger">{errors.password.message}</p>
            )}
          </div>
          <div className="space-y-2">
            <Label htmlFor="confirmPassword">Confirm password</Label>
            <Input
              id="confirmPassword"
              type="password"
              {...register("confirmPassword")}
              placeholder="........"
            />
            {errors.confirmPassword && (
              <p className="text-sm text-status-danger">{errors.confirmPassword.message}</p>
            )}
          </div>
          <Button className="w-full" type="submit">
            {registerMutation.isPending ? "Creating account..." : "Create account"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
