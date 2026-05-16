import { useEffect, useState } from "react";
import { Search } from "lucide-react";
import { useNavigate } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ROUTES } from "@/routes/paths";
import { cn } from "@/utils/cn";

export function GlobalSearchForm({
  initialValue = "",
  placeholder = "Destination, hotel, tour, restaurant",
  submitLabel = "Search",
  className,
  inputClassName,
  buttonClassName,
  buttonVariant = "default",
  buttonSize = "default"
}) {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState(initialValue);

  useEffect(() => {
    setSearchTerm(initialValue);
  }, [initialValue]);

  const handleSubmit = (event) => {
    event.preventDefault();

    const keyword = searchTerm.trim();
    if (!keyword) {
      navigate(ROUTES.search);
      return;
    }

    navigate(`${ROUTES.search}?keyword=${encodeURIComponent(keyword)}`);
  };

  return (
    <form className={cn("grid gap-3 md:grid-cols-[1fr_auto]", className)} onSubmit={handleSubmit}>
      <div className="relative">
        <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
        <Input
          aria-label="Search travel services"
          className={cn("pl-9", inputClassName)}
          placeholder={placeholder}
          value={searchTerm}
          onChange={(event) => setSearchTerm(event.target.value)}
        />
      </div>
      <Button
        className={buttonClassName}
        size={buttonSize}
        type="submit"
        variant={buttonVariant}
      >
        {submitLabel}
      </Button>
    </form>
  );
}
