/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html",
  "./src/**/*.{vue,js,ts,jsx,tsx}"],
  theme: {
    extend: {},
  },
  plugins: [
    require('daisyui'),
  ],
  daisyui: {
    themes: [
      {
        mytheme: {
          "primary": "#ffffff",
          "secondary":"#36454F",  // Set primary to white
          "input": "#ffffff",
              // Custom input background color
          // Define other colors as needed
        },
      },
      "light",  // You can still use the default light theme or other themes DaisyUI provides
    ],
  },
}

