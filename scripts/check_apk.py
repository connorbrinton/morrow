"""Check the actual APK for bundled lessons and absence of network permission."""
import os
from pathlib import Path
import subprocess
import sys
import zipfile
import yaml
from prepare_content import validate, UniqueLoader
apk = Path(sys.argv[1])
sdk = Path(os.environ.get('ANDROID_HOME') or os.environ.get('ANDROID_SDK_ROOT') or '')
aapt = sdk/'build-tools/35.0.0'/('aapt.exe' if os.name == 'nt' else 'aapt')
permissions = subprocess.check_output([str(aapt), 'dump', 'permissions', str(apk)], text=True)
assert 'android.permission.INTERNET' not in permissions, 'APK must not request internet access'
with zipfile.ZipFile(apk) as bundle:
    pack = validate(yaml.load(bundle.read('assets/content.yaml'), Loader=UniqueLoader))
print(f"Offline APK verified: {sum(len(w['days']) for w in pack['weeks'])} bundled lessons, all KJV passages, no INTERNET permission")
