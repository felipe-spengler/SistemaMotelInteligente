with open(r"E:\Infobac\InfoMotel\InfoAutomacaoDispositivo.exe", "rb") as f:
    data = f.read()

target_ascii = "%s%3.3d".encode("ascii")
target_utf16 = "%s%3.3d".encode("utf-16le")

idx = -1
is_utf16 = False

if target_ascii in data:
    idx = data.index(target_ascii)
    print(f"Target found in ASCII at byte index {idx}")
elif target_utf16 in data:
    idx = data.index(target_utf16)
    is_utf16 = True
    print(f"Target found in UTF-16 at byte index {idx}")

if idx != -1:
    # Extrai 2000 bytes em volta
    start = max(0, idx - 1000)
    end = min(len(data), idx + 1000)
    surround = data[start:end]
    
    # Extrai strings legíveis
    import re
    ascii_strings = re.findall(b"[ -~]{4,}", surround)
    utf16_strings = re.findall(b"(?:[ -~]\\x00){4,}", surround)
    
    print("\n--- ASCII STRINGS SURROUNDING TARGET ---")
    for s in ascii_strings:
        print(s.decode('ascii', errors='ignore'))
        
    print("\n--- UTF-16 STRINGS SURROUNDING TARGET ---")
    for s in utf16_strings:
        print(s.decode('utf-16le', errors='ignore'))
else:
    print("Target not found in binary.")
