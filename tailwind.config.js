/** @type {import('tailwindcss').Config} */
module.exports = {
	content: ["./src/main/**/*.cljs", "./src/tasks/**/*.cljs"],
	theme: {
		extend: {
			colors: {
				"notion-light": "#ffffff",
				"notion-dark": "#191919",
				"notion-subtle-light": "#FBFAF9",
				"notion-subtle-dark": "#202020",
			},
			boxShadow: {
				inner: "0 1px 1px 0 rgba(255, 255, 255, 0.3) inset",
			},
		},
	},
	plugins: [require("@tailwindcss/forms")],
}
