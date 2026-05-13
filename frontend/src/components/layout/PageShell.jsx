import { cn } from "@/utils/cn";

export function PageShell({ className, children }) {
  return <main className={cn("container shell-page", className)}>{children}</main>;
}
