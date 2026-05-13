import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

export function RegisterPage() {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Create account</CardTitle>
        <CardDescription>
          OTP and social-login ready registration entry for the TravelNest platform.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4 text-sm text-muted-foreground">
        <div className="rounded-md border p-4">Registration form module placeholder</div>
        <Button className="w-full">Create account</Button>
      </CardContent>
    </Card>
  );
}
