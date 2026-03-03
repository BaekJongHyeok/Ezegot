import urllib.request
import xml.etree.ElementTree as ET

url = 'http://openapi.seoul.go.kr:8088/6b684557416a6f6e3532634f584472/xml/SearchInfoBySubwayNameService/1/800/'
req = urllib.request.Request(url)
with urllib.request.urlopen(req) as response:
    root = ET.fromstring(response.read())

valid = 0
invalid = 0
for row in root.findall('.//row'):
    fr = row.find('FR_CODE').text
    name = row.find('STATION_NM').text
    line = row.find('LINE_NUM').text
    try:
        tt_url = f'http://openapi.seoul.go.kr:8088/6b684557416a6f6e3532634f584472/xml/SearchSTNTimeTableByFRCodeService/1/1/{fr}/1/1/'
        with urllib.request.urlopen(urllib.request.Request(tt_url)) as r:
            tt_root = ET.fromstring(r.read())
            code = tt_root.find('.//CODE').text
            if code == 'INFO-000':
                valid += 1
                if valid <= 5:
                    print(f'SUCCESS: {name} ({line}) FR: {fr}')
            else:
                invalid += 1
                if name == '강남':
                    # let's try 0 padding for gangnam
                    tt_url2 = f'http://openapi.seoul.go.kr:8088/6b684557416a6f6e3532634f584472/xml/SearchSTNTimeTableByFRCodeService/1/1/0{fr}/1/1/'
                    with urllib.request.urlopen(urllib.request.Request(tt_url2)) as r2:
                        tt_root2 = ET.fromstring(r2.read())
                        code2 = tt_root2.find('.//CODE').text
                        print(f'Gangnam zero padded 0{fr}: {code2}')
    except Exception as e:
        pass

print(f'\nTotal Valid: {valid}, Invalid: {invalid}')
