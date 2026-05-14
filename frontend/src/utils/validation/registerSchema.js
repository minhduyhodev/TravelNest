import { z } from "zod";

export const registerSchema = z
  .object({
    fullName: z.string().min(2, "Full name must be at least 2 characters").max(150),
    email: z.string().email("Invalid email address"),
    phone: z.string().max(20).optional(),
    preferredLang: z.enum(["vi", "en"]),
    password: z.string().min(8, "Password must be at least 8 characters").max(100),
    confirmPassword: z.string().min(8, "Please confirm your password")
  })
  .refine((values) => values.password === values.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"]
  });
