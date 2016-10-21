[한국어](https://github.com/isac322/OneDrive-API-java/blob/jackson-test/README.kor.md) | **English**


# [OneDrive](https://onedrive.live.com/) API for Java


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

- All OneDrive jobs are performed by `Client` object.
- A program can contain multiple different `Client` object.
- Basically `Client` object check expiration and refresh authorization. but it can done manually.
- All parameters that pass to `Client`'s constructor can obtain if you fallow [OneDrive app instruction of authentication](https://dev.onedrive.com/app-registration.htm). 

```java
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

- For now, it can conduct by only ID.
- `FolderItem` and `FileItem` are represent folder and file respectively.
- `FolderItem` and `FileItem` are child class of `BaseItem`.

```java
// assume that Client object is already obtained
// get root directory
FolderItem root = client.getRootDir();


// get folder by ID
FolderItem folder = client.getFolder("XXXXXXXXXXXXXXXX!XXXX");

// get file by ID
FileItem folder = client.getFile("XXXXXXXXXXXXXXXX!XXXX");

// or if you don't know whether ID is file or folder
BaseItem folder = client.getItem("XXXXXXXXXXXXXXXX!XXXX");
```


### 3. get children of a folder

- `FolderItem` are `Iterable`. (it will returns child as `BaseItem`)
- Basically if `FolderItem` object is fetched by `Client`'s method `getFolder` or `getRootDir`, the object automatically fetchs children too. **(If children list is very long, it could take long time)**
- If you call `FolderItem`'s method `getAllChildren` or `getFolderChildren` or `getFileChildren`, you can get `List` of all children, only folder children, only file children respectively.
- if you call above methods, it will load children data and cache it. so **First call of those methods can take long time.**

```java
// assume that Client object is already obtained
FolderItem root = client.getRootDir();

List<BaseItem> children = root.getAllChildren();
List<FolderItem> folderChildren = root.getFolderChildren();
List<FileItem> fileChildren = root.getFileChildren();
```


### 4. Create folder

- For now, it can only create by parent's `FolderItem` object.

```java
// assume that Client object is already obtained
FolderItem root = client.getRootDir();

String newId = root.createFolder("test");
```


### 5. Copy folder or file

- For now, it can only copy by source item's object.

```java
// assume that Client object is already obtained
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

- For now, it can only download by file item's object.
- For now, only supports synchronous way. (async way will support soon)

```java
import java.nio.file.Paths;

// assume that Client object is already obtained
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