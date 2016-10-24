[한국어](https://github.com/isac322/OneDrive-API-java/blob/jackson-test/README.kor.md) | **English**


# [OneDrive](https://onedrive.live.com/) API for Java

purse fast, easy to use, intuitive API.


### Supported Operation

- auto login authorization check and refresh
- fetching metadata of folder, file (by id)
- folder or file's metadata (size, name, path, children list and etc.)
- downloading file (sync)
- delete, copy, move, change metadata(name, description) of folder or file
- creating folder (only by parent object)
- [Facets](https://dev.onedrive.com/facets/facets.htm) that OneDrive support like image, video..
- inquiring shared folder
- basic [RemoteItem](https://dev.onedrive.com/misc/working-with-links.htm) handling
- inquiring [Drives](https://dev.onedrive.com/resources/drive.htm)



### TODO

- downloading file (async: almost complete)
- searching file or folder (by name or content)
- fetching metadata of folder, file (by path)
- creating folder (solely by path or id, without parent object)
- creating file and upload it (async)
- sharing folder or file 
- downloading whole folder
- Maven
- documentation
- support custom redirect url when login
- REST-api response error handling


### Environment

- JRE7


### Dependency

*These are already included in directory 'lib'.*

- [Jackson](https://github.com/FasterXML/jackson)
- [Lombok](https://projectlombok.org/)
- [netty](http://netty.io/)


-----------------

## Simple example


### 1. Construct `Client` object

- All OneDrive jobs are performed via `Client` object.
- A program can contain multiple different `Client` object.
- Basically `Client` object check expiration and refresh authorization automatically. but it can be done manually.
- All parameters that pass to `Client`'s constructor can obtain if you fallow [OneDrive app instruction of authentication](https://dev.onedrive.com/app-registration.htm). 

```java
import org.onedrive.Client;

String clientId = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
String[] scope = {"onedrive.readwrite", "offline_access", "onedrive.appfolder"};
String redirectURL = "http://localhost:8080/";
String clientSecret = "xxxxxxxxxxxxxxxxxxxxxxx";

// auto login
Client client = new Client(clientId, scope, redirectURL, clientSecret);
// self login
Client client = new Client(clientId, scope, redirectURL, clientSecret, false);
client.login();
```


### 2. Folder, file fetch

- For now, it can conduct only via ID.
- `FolderItem` and `FileItem` are represent folder and file respectively.
- `FolderItem` and `FileItem` are child class of `BaseItem`.

```java
import org.onedrive.container.items.FolderItem;
import org.onedrive.container.items.BaseItem;

// assume that Client object is already constructed
// get root directory
FolderItem root = client.getRootDir();


// get folder by ID
FolderItem folder = client.getFolder("XXXXXXXXXXXXXXXX!XXXX");

// get file by ID
FileItem file = client.getFile("XXXXXXXXXXXXXXXX!XXXX");

// or if you don't know whether ID is file or folder
BaseItem item = client.getItem("XXXXXXXXXXXXXXXX!XXXX");
```


### 3. get children of a folder

- `FolderItem` are `Iterable`. (it will returns child as `BaseItem`)
- Basically if `FolderItem` object is fetched by `Client`'s method `getFolder` or `getRootDir`, the object automatically fetchs children too. **(If children list is very long, it could take long time)**
- If you call `FolderItem`'s method `getAllChildren` or `getFolderChildren` or `getFileChildren`, you can get `List` of all children, only folder children, only file children respectively.
- if you call above methods, it will load children data and cache it. so **First call of those methods can take long time.**

```java
import org.onedrive.container.items.*;
// assume that Client object is already constructed
FolderItem root = client.getRootDir();

List<BaseItem> children = root.getAllChildren();
List<FolderItem> folderChildren = root.getFolderChildren();
List<FileItem> fileChildren = root.getFileChildren();
```


### 4. Create folder

- For now, it can create only via parent's `FolderItem` object.

```java
import org.onedrive.container.items.FolderItem;

// assume that Client object is already constructed
FolderItem root = client.getRootDir();

String newId = root.createFolder("test");
```


### 5. Copy folder or file

- For now, it can copy only via source item's object.

```java
import org.onedrive.container.items.*;

// assume that Client object is already constructed
BaseItem item = client.getItem("XXXXXXXXXXXXXXXX!XXXX");

FolderItem destination = client.getFolder("XXXXXXXXXXXXXXXX!XXXX");

// direct copy
item.copyTo(destination);
// direct copy with new name
item.copyTo(destination, "newName");


// copy by reference object
item.copyTo(destination.newRerence());
// copy by reference object with new name
item.copyTo(destination.newRerence(), "newName");


// copy by path string
item.copyToPath(destination.getPath());
// copy by path string with new name
item.copyToPath(destination.getPath(), "newName");


// copy by id string
item.copyToId(destination.getId());
// copy by id string with new name
item.copyToId(destination.getId(), "newName");
```


### 6. Download file

- For now, it can download only via `FileItem`'s object.
- For now, only supports synchronous way. (async way will be supported soon)

```java
import java.nio.file.Paths;
import org.onedrive.container.items.FileItem;

// assume that Client object is already constructed
FileItem file = client.getFile("XXXXXXXXXXXXXXXX!XXXX");

String path = "/home/isac322/download";

// download by system path string with original file name
file.download(path);

// download by system path string with new name
file.download(path, "newName");


// download by path object with original file name
file.download(Paths.get(path));

// download by path object with new name
file.download(Paths.get(path), "newName");
```

### 7. Move folder or file

- For now, it can move only via source item's object.

```java
import org.onedrive.container.items.BaseItem;

// assume that Client object is already constructed
BaseItem item = client.getItem("XXXXXXXXXXXXXXXX!XXXX");

FolderItem destination = client.getFolder("XXXXXXXXXXXXXXXX!XXXX");

// direct move
item.moveTo(destination);


// move by reference object
item.moveTo(destination.newRerence());


// move by path string
item.moveToPath(destination.getPath());


// move by id string
item.moveToId(destination.getId());
```

### 8. Update folder or file's metadata & Refresh

- `setName` and `setDescription` of `BaseItem` are lazy-operation. Actual modification will be adjusted after call `update`.
- `update` will upload local changes and update all variable with fetched latest metadata. 
- That is, if `update` is invoked, all variable can be changed, even if the current program did not modify the variables.

```java
import org.onedrive.container.items.BaseItem;

// assume that Client object is already constructed
BaseItem item = client.getItem("XXXXXXXXXXXXXXXX!XXXX");

// change item's name and flush to server.
item.setName("new name");
item.update();


// change item's description and flush to server.
item.setDescription("blah blah");
item.update();


// refresh item's all variable to latest value
item.update();
```