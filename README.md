# Cordova FileChooser Plugin

Requires Cordova >= 2.8.0

## Install with Cordova CLI
	$ cordova plugin add https://github.com/rastafan/cordova-filechooser

## Install with Plugman
	$ plugman --platform android --project /path/to/project \ 
		--plugin https://github.com/rastafan/cordova-filechooser

## API

```javascript
fileChooser.open(filter, successCallback, failureCallback); // with mime filter

fileChooser.open(successCallback. failureCallback); // without mime filter
```

### Filter (Optional)

```javascript
{ "mime": "application/pdf" }  // text/plain, image/png, image/jpeg, audio/wav etc
  // defaults to "*/*"
  {"mime": "*/*", "extraMIME" : "application/pdf,image/png,image/jpeg,video/mp4,video/mpg"}
```

The success callback gets the uri of the selected file

```javascript
fileChooser.open(function(data) {
  alert(data.uri);
  alert(data.name);
  alert(data.mime);
});
```

## Screenshot

![Screenshot](filechooser.png "Screenshot")

## Supported Platforms

- Android
- Windows (UWP)

TODO rename `open` to pick, select, or choose.
