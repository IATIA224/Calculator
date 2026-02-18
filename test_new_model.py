#!/usr/bin/env python3
import requests
import json

GEMINI_API_KEY = "AIzaSyC16ssjvblqk_SUE4py7t035zlB3VZ4hEQ"
GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

payload = {
    "contents": [{"parts": [{"text": "Say 'API is working!' in one sentence"}]}],
    "generationConfig": {"temperature": 0.2}
}

url = f"{GEMINI_URL}?key={GEMINI_API_KEY}"

print("Testing gemini-2.5-flash model...\n")

try:
    response = requests.post(url, json=payload, timeout=30)
    
    if response.status_code == 200:
        print("✅ SUCCESS! gemini-2.5-flash is working!")
        data = response.json()
        text = data["candidates"][0]["content"]["parts"][0]["text"]
        print(f"\n💬 Response: {text}")
    else:
        print(f"❌ Error {response.status_code}")
        print(json.dumps(response.json(), indent=2))
except Exception as e:
    print(f"❌ Error: {e}")
