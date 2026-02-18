#!/usr/bin/env python3
"""
List all available Gemini models.
"""

import requests
import json

GEMINI_API_KEY = "AIzaSyC16ssjvblqk_SUE4py7t035zlB3VZ4hEQ"
LIST_MODELS_URL = "https://generativelanguage.googleapis.com/v1beta/models"

print("=" * 70)
print("Fetching available Gemini models...")
print("=" * 70)

try:
    response = requests.get(
        f"{LIST_MODELS_URL}?key={GEMINI_API_KEY}",
        timeout=10
    )
    
    print(f"\n✅ HTTP Status: {response.status_code}\n")
    
    if response.status_code == 200:
        data = response.json()
        
        if "models" in data:
            print(f"Found {len(data['models'])} available models:\n")
            
            for model in data["models"]:
                name = model.get("name", "Unknown")
                display_name = model.get("displayName", "")
                supported_methods = model.get("supportedGenerationMethods", [])
                
                # Clean up the name (remove "models/" prefix)
                model_id = name.replace("models/", "")
                
                print(f"📌 {model_id}")
                if display_name:
                    print(f"   Display: {display_name}")
                print(f"   Methods: {', '.join(supported_methods)}")
                print()
        else:
            print("No models found in response")
            print(json.dumps(data, indent=2))
    else:
        print(f"❌ Error {response.status_code}")
        print(json.dumps(response.json(), indent=2))

except Exception as e:
    print(f"❌ Error: {e}")
