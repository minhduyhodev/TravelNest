import { AppProviders } from "@/app/providers/AppProviders";
import { AppRouter } from "@/routes/AppRouter";
import { BrowserRouter } from "react-router-dom";

export default function App() {
  return (
    <AppProviders>
      <BrowserRouter>
        <AppRouter />
      </BrowserRouter>
    </AppProviders>
  );
}
