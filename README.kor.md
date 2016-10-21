**한국어** | [English](https://github.com/isac322/OneDrive-API-java/blob/jackson-test/Readme.md)


# [OneDrive](https://onedrive.live.com/) API for Java

빠르고, 쓰기 쉽고, 불필요한 과정이 없는 API를 지향합니다.


### 간략한 지원기능

- 폴더, 파일 로드 (by id)
- 폴더, 파일 정보 확인 (크기, 이름, 경로, 폴더 내부 목록 등등)
- 파일 다운로드 (sync)
- 폴더, 파일 정보 변경, 삭제, 복사
- 폴더 생성 (브모 객체를 통해서만)
- 이미지, 비디오, 등등 OneDrive에서 지원하는 [Facets](https://dev.onedrive.com/facets/facets.htm)
- 공유 폴더 조회
- 간단한 [RemoteItem](https://dev.onedrive.com/misc/working-with-links.htm) handling
- [Drive](https://dev.onedrive.com/resources/drive.htm) 조회

##### ~~_구현해 놓고 빠트린게 있을수도있..._~~



### 앞으로 추가할 기능

- 파일 다운로드 (async: almost complete)
- 파일 or 폴더 검색 (by name or content)
- 폴더, 파일 로드 (by path)
- 폴더, 파일 이동
- 폴더 생성 (부모 객체 없이 경로를 통해서만)
- 파일 생성, 내용 업로드 (async)
- 공유 기능
- 폴더채로 다운
- Maven
- 문서화
- support custom redirect url when login
- REST-api response error handling

### Environment

- JRE7


### Dependency

*lib 폴더에 이미 포함됨*

- [Jackson](https://github.com/FasterXML/jackson)
- [Lombok](https://projectlombok.org/)
- [netty](http://netty.io/)


-----------------

## 간단한 예제


### 1. `Client`객체 생성

- 모든 작업은 `Client`객체를 통해 이뤄진다.
- 복수개의 `Client`객체를 생성할 수 있다.
- 자동 혹은 수동으로 `Client`객체의 만료(expiration)을 갱신할 수 있다.
- 생성에 사용되는 변수들은 [OneDrive app 인증 설명](https://dev.onedrive.com/app-registration.htm)을 따라하면 얻을 수 있다. 

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


### 2. 파일, 폴더 로드

- 현재까지는 ID를 통해서만 가능함.
- 폴더는 `FolderItem`, 파일은 `FileItem`객체로 나눔.
- `FolderItem`과 `FileItem`은 모두 `BaseItem`의 자식 클래스임.

```java
// Client는 생성 되어있다고 가정


// get root directory
FolderItem root = client.getRootDir();


// get folder by ID
FolderItem folder = client.getFolder("XXXXXXXXXXXXXXXX!XXXX");

// get file by ID
FileItem folder = client.getFile("XXXXXXXXXXXXXXXX!XXXX");

// or if you don't know whether ID is file or folder
BaseItem folder = client.getItem("XXXXXXXXXXXXXXXX!XXXX");
```


### 3. 폴더의 자식 조회

- `FolderItem`는 `Iterable`함. (자식 아아이템들을 `BaseItem`으로 반환)
- 기본적으로 `FolderItem`객체가 `Client`의 `getFolder`, `getRootDir`등으로 생성될 경우 자식 목록도 자동적으로 불러옴. **(폴더 자식 목록이 길다면 객체 생성이 오래걸릴 수도 있음)**
- `FolderItem`의 `getAllChildren`, `getFolderChildren`, `getFileChildren`를 호출하면 각각 모든 자식, 폴더인 자식, 파일인 자식 `List`를 얻을 수 있음.
- 위의 메소드들은 호출시 자식정보를 load하고 caching한 후 반환하기 떄문에 **첫 호출은 오래걸릴 수도 있음.**

```java
// Client는 생성 되어있다고 가정

FolderItem root = client.getRootDir();

List<BaseItem> children = root.getAllChildren();
List<FolderItem> folderChildren = root.getFolderChildren();
List<FileItem> fileChildren = root.getFileChildren();
```


### 4. 폴더 생성

- 현재까지는 부모 `FolderItem`을 통해서만 생성 가능.

```java
// Client는 생성 되어있다고 가정

FolderItem root = client.getRootDir();

String newId = root.createFolder("test");
```


### 5. 폴더, 파일 복사

- 현재까지는 복사하고싶은 아이템의 객체를 통해서만 가능.

```java
// Client는 생성 되어있다고 가정

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


### 6. 파일 다운로드

- 현재까지는 다운로드 받고싶은 아이템의 객치를 통해서만 가능.
- 현재까지는 synchronous한 방식으로만 가능. (async도 조만간 완료)

```java
import java.nio.file.Paths;
// Client는 생성 되어있다고 가정

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