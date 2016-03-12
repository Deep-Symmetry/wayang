# wayang

A Java library for displaying images on the
[Ableton Push 2](https://www.ableton.com/en/push/) instrument. The
project name, which refers to
[Javanese shadow puppet performances](https://en.wikipedia.org/wiki/Wayang),
was chosen because it ties together the concepts Java, Push, and
images.

[![License](https://img.shields.io/badge/License-Eclipse%20Public%20License%201.0-blue.svg)](#license)

## Usage

Wayang bundles
[usb4java](http://usb4java.org/quickstart/javax-usb.html) to
communicate with the Push. For that to work, you need to have
[libusb](http://libusb.info) installed on your system. If you are on
Linux it is likely already installed, or you can follow their
installation instructions. On the Mac, I recommend using
[Homebrew](http://brew.sh) for all such needs; once it&rsquo;s
installed, you can simply run `brew install libusb`.

> If you are already using the
> [Open Lighting Architecture](https://www.openlighting.org/ola/),
> perhaps through
> [Afterglow](https://github.com/brunchboy/afterglow#afterglow), then
> libusb was installed as part of installing OLA.

More documentation coming soon!

## License

<img align="right" alt="Deep Symmetry" src="assets/DS-logo-bw-200-padded-left.png">
Copyright © 2016 [Deep Symmetry, LLC](http://deepsymmetry.org)

Distributed under the
[Eclipse Public License 1.0](http://opensource.org/licenses/eclipse-1.0.php).
By using this software in any fashion, you are agreeing to be bound by
the terms of this license. You must not remove this notice, or any
other, from this software. A copy of the license can be found in
[assets/epl-v10.html](https://rawgit.com/brunchboy/wayang/master/assets/epl-v10.html)
within this project.
