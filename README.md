# Java - 多人聊天室與資料查詢

## 功能簡介

* 聊天區

    * 可自訂使用者暱稱
    * 可顯示聊天室成員加入/離開訊息
    * 可使用按鈕傳送訊息
* 資料查詢

    * 可查詢某縣市未來一週天氣
    * 可查詢某縣市空氣品質
    * 可查詢即時新聞

> 開發工具：**eclipse**
> 
> 開發語言：**Java**
> 
> 使用技術：GUI、Thread、Socket、URL Connection

## 成果展示
<img src="/img/demo1.1.jpg" width="60%" height="60%" />

<img src="/img/demo1.2.jpg" width="60%" height="60%" />

<img src="/img/demo1.3.jpg" width="60%" height="60%" />

<img src="/img/demo1.4.jpg" width="60%" height="60%" />

## 資料來源
- 一週天氣：[PChome氣象](http://news.pchome.com.tw/weather/taiwan)
- 空氣品質：[行政院環保署環境資料開放平臺](https://data.epa.gov.tw/dataset/aqx_p_432)
- 即時新聞：[聯合新聞網](https://udn.com/news/breaknews/1)


### 需自行設定項目

- 查詢空氣品質需自行設定`api_key`，取得方式請至[行政院環保署環境資料開放平臺](https://data.epa.gov.tw/paradigm)。

```java
URL url = new URL("https://data.epa.gov.tw/api/v1/aqx_p_432?offset=0&limit=100&api_key={api_key}")
```
