import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useMutation } from "@tanstack/react-query";
import { authApi } from "../api/authApi";
import { useAuthStore } from "../store/authStore";

export default function LoginPage() {
  const navigate = useNavigate();

  const setTokens = useAuthStore((s) => s.setTokens);
  const setUser = useAuthStore((s) => s.setUser);

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const mutation = useMutation({
    mutationFn: authApi.login,

    onSuccess: ({ data }) => {
      setTokens(data.accessToken, data.refreshToken);
      setUser(data.user);
      navigate("/dashboard");
    },
  });

  return (
    <div className="min-h-screen bg-slate-100 flex items-center justify-center">
      <form
        onSubmit={(e) => {
          e.preventDefault();
          mutation.mutate({ email, password });
        }}
        className="bg-white p-8 rounded-xl shadow w-full max-w-md space-y-5"
      >
        <h1 className="text-3xl font-bold text-center">
          VaultFlow
        </h1>

        <input
          className="w-full border rounded-lg px-4 py-2"
          placeholder="Email"
          type="email"
          value={email}
          onChange={(e)=>setEmail(e.target.value)}
        />

        <input
          className="w-full border rounded-lg px-4 py-2"
          placeholder="Password"
          type="password"
          value={password}
          onChange={(e)=>setPassword(e.target.value)}
        />

        <button
          className="w-full bg-indigo-600 text-white py-2 rounded-lg"
        >
          {mutation.isPending ? "Signing in..." : "Login"}
        </button>

        <p className="text-center text-sm">
          Don't have an account?
          <Link
            to="/register"
            className="text-indigo-600 ml-2"
          >
            Register
          </Link>
        </p>
      </form>
    </div>
  );
}