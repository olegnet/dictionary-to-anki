## This is a small useful tool for adding new words to Anki application on desktop

It uses [Compose Multiplatform](https://github.com/JetBrains/compose-jb) for UI, [Ktor](https://ktor.io/) for networking,
[Dictionary API](https://yandex.com/dev/dictionary) for dictionary requests and
[Anki-Connect](https://foosoft.net/projects/anki-connect) to connect to [Anki](https://apps.ankiweb.net) application

## Configuration

Keys are expected to be in `$HOME/.config/dict2anki/keys.properties` file

`dictionary=...` [your key from https://yandex.com/dev/dictionary/](https://yandex.com/dev/dictionary/)

`anki=...`       [optional key for Anki Connect](https://foosoft.net/projects/anki-connect/)


Settings are saved to `$HOME/.config/dict2anki/settings.json`

## Notes for MacOS Users from Anki Connect documentation

Don't forget to run this commands once and restart Anki

```
defaults write net.ankiweb.dtop NSAppSleepDisabled -bool true
defaults write net.ichi2.anki NSAppSleepDisabled -bool true
defaults write org.qt-project.Qt.QtWebEngineCore NSAppSleepDisabled -bool true
```

## License

    Copyright 2022 Oleg Okhotnikov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
