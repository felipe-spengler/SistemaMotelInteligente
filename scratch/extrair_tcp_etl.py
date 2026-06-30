import os
import sys
import win32evtlog
import xml.etree.ElementTree as ET

etl_path = r"C:\Users\Felipe\Documents\trace5361_extracted\report.etl"
output_path = r"C:\Users\Felipe\Documents\pacotes_porta_5361.txt"

print("==================================================")
print("=== EXTRAINDO PAYLOADS TCP DA PORTA 5361 (XML) ===")

query = '*[System[(EventID=1001)]]'
handle = win32evtlog.EvtQuery(etl_path, win32evtlog.EvtQueryFilePath | win32evtlog.EvtQueryForwardDirection, query)

packet_count = 0
found_count = 0

# A porta 5361 em decimal tem representações de bytes 20 e 241 (0x14 e 0xF1)
with open(output_path, "w", encoding="utf-8") as out:
    out.write("=== EXTRAÇÃO DE PAYLOADS TCP (XML PARSER) ===\n\n")
    
    while True:
        events = win32evtlog.EvtNext(handle, 50)
        if not events:
            break
            
        for event in events:
            packet_count += 1
            # EvtRenderEventXml eh muito mais estavel no pywin32 do que renderizar valores diretamente
            xml_str = win32evtlog.EvtRender(event, win32evtlog.EvtRenderEventXml)
            
            # Encontra os valores de dados brutos das propriedades dentro do XML
            # O formato do NDIS PacketCapture contem os bytes em elementos <Data>
            try:
                root = ET.fromstring(xml_str)
                # O namespace padrao do Evento
                ns = {'ns': 'http://schemas.microsoft.com/win/2004/08/events/event'}
                data_elements = root.findall('.//ns:Data', ns)
                
                # Coleta todos os valores inteiros das tags <Data>
                bytes_list = []
                for elem in data_elements:
                    text = elem.text
                    if text and text.isdigit():
                        bytes_list.append(int(text))
                
                # Procura pela porta 5361 (0x14 = 20, 0xF1 = 241)
                has_port = False
                for i in range(len(bytes_list) - 1):
                    if (bytes_list[i] == 20 and bytes_list[i+1] == 241) or (bytes_list[i] == 241 and bytes_list[i+1] == 20):
                        has_port = True
                        break
                
                if has_port:
                    found_count += 1
                    hex_str = " ".join([f"{b:02X}" for b in bytes_list])
                    ascii_str = "".join([chr(b) if 32 <= b <= 126 else "." for b in bytes_list])
                    
                    # Tenta extrair strings Unicode (UTF-16LE)
                    payload_bytes = bytes(bytes_list)
                    utf16_str = ""
                    try:
                        utf16_str = payload_bytes.decode('utf-16le', errors='ignore').replace('\x00', '')
                    except:
                        pass
                        
                    log_msg = f"PACOTE {found_count} (Tamanho: {len(bytes_list)} bytes):\n"
                    log_msg += f"  HEX:   {hex_str}\n"
                    log_msg += f"  ASCII: {ascii_str}\n"
                    if utf16_str.strip():
                        log_msg += f"  UTF16: {utf16_str.strip()}\n"
                    log_msg += "-" * 80 + "\n"
                    
                    out.write(log_msg)
                    print(f"Encontrado pacote {found_count} com {len(bytes_list)} bytes.")
            except Exception as ex:
                # Caso ocorra erro de parse do XML, ignora o frame e continua
                pass

print(f"\nConcluído! Total de pacotes de dados analisados: {packet_count}.")
print(f"Total da porta 5361 encontrados: {found_count}.")
print(f"Log gravado em: {output_path}")
print("==================================================")
