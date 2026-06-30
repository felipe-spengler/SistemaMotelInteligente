with open(r"E:\Infobac\InfoMotel\Base\BASE.FDB", "rb") as f:
    data = f.read()

target = b"T002PLA"
if target in data:
    idx = data.index(target)
    print(f"T002PLA found at byte index {idx}")
    
    # Extrai 2000 bytes em volta
    start = max(0, idx - 1000)
    end = min(len(data), idx + 1000)
    surround = data[start:end]
    
    # Extrai strings legíveis
    import re
    ascii_strings = re.findall(b"[A-Z0-9_$#]{3,}", surround)
    
    print("\n--- STRINGS AO REDOR DE T002PLA ---")
    for s in ascii_strings:
        print(s.decode('ascii', errors='ignore'))
else:
    print("T002PLA not found in database.")
