import re

with open('app/src/main/res/values/strings.xml', 'r') as f:
    content = f.read()

content = content.replace("YOUR_WEB_CLIENT_ID_HERE", "679867689390-m93ujgcvjc9pufevtsoqf33fv1t2gnop.apps.googleusercontent.com")

with open('app/src/main/res/values/strings.xml', 'w') as f:
    f.write(content)

