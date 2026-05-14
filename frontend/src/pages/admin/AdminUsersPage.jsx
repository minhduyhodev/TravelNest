import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { createStaffAccount, fetchStaffAccounts, updateUserStatus } from "@/api/users";
import { queryKeys } from "@/api/queryKeys";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

const emptyStaffForm = {
  fullName: "",
  email: "",
  phone: "",
  password: ""
};

const statusToneMap = {
  ACTIVE: "bg-emerald-50 text-emerald-700 border-emerald-200",
  INACTIVE: "bg-slate-100 text-slate-700 border-slate-200",
  BANNED: "bg-red-50 text-red-700 border-red-200",
  UNVERIFIED: "bg-amber-50 text-amber-700 border-amber-200"
};

const validationFieldLabels = {
  fullName: "Full name",
  email: "Email",
  phone: "Phone",
  password: "Temporary password"
};

export function AdminUsersPage() {
  const queryClient = useQueryClient();
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [staffForm, setStaffForm] = useState(emptyStaffForm);
  const [pageMessage, setPageMessage] = useState("");

  const normalizedStatusFilter = useMemo(
    () => (statusFilter === "ALL" ? undefined : statusFilter),
    [statusFilter]
  );

  const staffQuery = useQuery({
    queryKey: queryKeys.users.staff(normalizedStatusFilter),
    queryFn: () => fetchStaffAccounts(normalizedStatusFilter)
  });

  const createStaffMutation = useMutation({
    mutationFn: createStaffAccount,
    onSuccess: async () => {
      setPageMessage("Staff account created successfully.");
      setStaffForm(emptyStaffForm);
      await queryClient.invalidateQueries({ queryKey: queryKeys.users.staffRoot });
    }
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ userId, status }) => updateUserStatus(userId, { status }),
    onSuccess: async (_, variables) => {
      setPageMessage(`Staff status updated to ${variables.status}.`);
      await queryClient.invalidateQueries({ queryKey: queryKeys.users.staffRoot });
    }
  });

  const handleCreateStaff = (event) => {
    event.preventDefault();
    setPageMessage("");
    createStaffMutation.mutate(staffForm);
  };

  const mutationError = createStaffMutation.error?.message || updateStatusMutation.error?.message;
  const createStaffValidationErrors = createStaffMutation.error?.validationErrors;

  return (
    <div className="grid gap-6 xl:grid-cols-[380px_1fr]">
      <Card>
        <CardHeader>
          <CardTitle>Create staff account</CardTitle>
          <CardDescription>
            Minimal Phase 1 admin flow to onboard operations staff into the protected dashboard.
          </CardDescription>
        </CardHeader>
        <CardContent>
          {pageMessage && (
            <div className="mb-4 rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
              {pageMessage}
            </div>
          )}
          {mutationError && (
            <div className="mb-4 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              <p>{mutationError}</p>
              {createStaffValidationErrors && (
                <ul className="mt-2 list-disc pl-5">
                  {Object.entries(createStaffValidationErrors).map(([field, message]) => (
                    <li key={field}>
                      <span className="font-medium">{validationFieldLabels[field] || field}:</span> {message}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          )}
          <form className="space-y-4" onSubmit={handleCreateStaff}>
            <div className="space-y-2">
              <Label htmlFor="staff-full-name">Full name</Label>
              <Input
                id="staff-full-name"
                value={staffForm.fullName}
                onChange={(event) =>
                  setStaffForm((current) => ({ ...current, fullName: event.target.value }))
                }
                required
              />
              {createStaffValidationErrors?.fullName && (
                <p className="text-sm text-status-danger">{createStaffValidationErrors.fullName}</p>
              )}
            </div>
            <div className="space-y-2">
              <Label htmlFor="staff-email">Email</Label>
              <Input
                id="staff-email"
                type="email"
                value={staffForm.email}
                onChange={(event) =>
                  setStaffForm((current) => ({ ...current, email: event.target.value }))
                }
                required
              />
              {createStaffValidationErrors?.email && (
                <p className="text-sm text-status-danger">{createStaffValidationErrors.email}</p>
              )}
            </div>
            <div className="space-y-2">
              <Label htmlFor="staff-phone">Phone</Label>
              <Input
                id="staff-phone"
                value={staffForm.phone}
                onChange={(event) =>
                  setStaffForm((current) => ({ ...current, phone: event.target.value }))
                }
              />
              {createStaffValidationErrors?.phone && (
                <p className="text-sm text-status-danger">{createStaffValidationErrors.phone}</p>
              )}
            </div>
            <div className="space-y-2">
              <Label htmlFor="staff-password">Temporary password</Label>
              <Input
                id="staff-password"
                type="password"
                value={staffForm.password}
                onChange={(event) =>
                  setStaffForm((current) => ({ ...current, password: event.target.value }))
                }
                required
              />
              {createStaffValidationErrors?.password && (
                <p className="text-sm text-status-danger">{createStaffValidationErrors.password}</p>
              )}
            </div>
            <Button className="w-full" type="submit">
              {createStaffMutation.isPending ? "Creating staff..." : "Create staff"}
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
          <div>
            <CardTitle>Staff accounts</CardTitle>
            <CardDescription>
              Review current operations users and toggle their availability for dashboard access.
            </CardDescription>
          </div>
          <div className="space-y-2">
            <Label htmlFor="staff-status-filter">Status</Label>
            <select
              id="staff-status-filter"
              className="flex h-10 min-w-40 rounded-md border border-input bg-background px-3 py-2 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
            >
              <option value="ALL">All staff</option>
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
              <option value="BANNED">Banned</option>
              <option value="UNVERIFIED">Unverified</option>
            </select>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          {staffQuery.isLoading && (
            <div className="rounded-md border border-dashed p-4 text-sm text-muted-foreground">
              Loading staff accounts...
            </div>
          )}
          {staffQuery.isError && (
            <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {staffQuery.error.message}
            </div>
          )}
          {!staffQuery.isLoading && staffQuery.data?.length === 0 && (
            <div className="rounded-md border border-dashed p-4 text-sm text-muted-foreground">
              No staff account matches the current filter yet.
            </div>
          )}
          {staffQuery.data?.map((staff) => {
            const nextStatus = staff.status === "ACTIVE" ? "INACTIVE" : "ACTIVE";
            const actionLabel = staff.status === "ACTIVE" ? "Deactivate" : "Activate";

            return (
              <div
                key={staff.id}
                className="flex flex-col gap-4 rounded-xl border p-4 md:flex-row md:items-center md:justify-between"
              >
                <div className="space-y-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <p className="font-medium">{staff.fullName}</p>
                    <span
                      className={`rounded-full border px-2 py-1 text-xs font-medium ${statusToneMap[staff.status] || "bg-slate-100 text-slate-700 border-slate-200"}`}
                    >
                      {staff.status}
                    </span>
                  </div>
                  <p className="text-sm text-muted-foreground">{staff.email}</p>
                  <p className="text-sm text-muted-foreground">{staff.phone || "No phone number"}</p>
                </div>
                <div className="flex flex-wrap gap-2">
                  <Button
                    type="button"
                    variant={staff.status === "ACTIVE" ? "outline" : "default"}
                    size="sm"
                    onClick={() => updateStatusMutation.mutate({ userId: staff.id, status: nextStatus })}
                  >
                    {updateStatusMutation.isPending ? "Updating..." : actionLabel}
                  </Button>
                </div>
              </div>
            );
          })}
        </CardContent>
      </Card>
    </div>
  );
}
