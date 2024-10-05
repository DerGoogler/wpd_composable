# WiFi Password Viewer for MMRL

An inspired project from [veez21](https://github.com/veez21)'s [wpd gist](https://gist.github.com/veez21/4f2541d271809864411e3ffbbe8e3df9), ported to MMRL.
This project is also a example to show how powerful MMRL's ModConf feature is.

> This module does not extract passwords from unauthorized wifi's. This module is for education purpose only

## Screenshots

<p>
  <img src="./assets/1.webp" alt="Screenshot 1 of WPD" width="32%" />
  <img src="./assets/2.webp" alt="Screenshot 2 of WPD" width="32%" />
  <img src="./assets/3.webp" alt="Screenshot 3 of WPD" width="32%" />
  <img src="./assets/4.webp" alt="Screenshot 4 of WPD" width="32%" />
  <img src="./assets/5.webp" alt="Screenshot 5 of WPD" width="32%" />
</p>


## TODO

- [ ] Build full functional library 


## Notes

- Dex files should have the permission `444` to avoid conflicts on Android 14
- Chars with in this regex (`[^a-zA-Z0-9._]`) will be replaced with `_`