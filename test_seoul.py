import urllib.request
import xml.etree.ElementTree as ET

url = "http://openapi.seoul.go.kr:8088/6b684557416a6f6e3532634f584472/xml/SearchInfoBySubwayNameService/1/800/"
req = urllib.request.Request(url)
with urllib.request.urlopen(req) as response:
    root = ET.fromstring(response.read())

print(f"Total entries: {len(root.findall('.//row'))}")
for row in root.findall('.//row')[:10]:
    name = row.find('STATION_NM').text
    cd = row.find('STATION_CD').text
    fr = row.find('FR_CODE').text
    line = row.find('LINE_NUM').text
    print(f"Name: {name}, CD: {cd}, FR: {fr}, Line: {line}")
