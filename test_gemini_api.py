#!/usr/bin/env python3
"""
Test script to verify Google Gemini API is working.
Tests the API endpoint independently from the Android app.
"""

import requests
import json
import base64
from pathlib import Path

# Your API key
GEMINI_API_KEY = "AIzaSyC16ssjvblqk_SUE4py7t035zlB3VZ4hEQ"

# Test endpoint
GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

def test_api():
    """Test basic text request to verify API is working."""
    print("=" * 60)
    print("Testing Google Gemini API")
    print("=" * 60)
    
    # Simple text request (no image)
    payload = {
        "contents": [
            {
                "parts": [
                    {
                        "text": "Say 'API is working!' in JSON format: {\"status\": \"working\"}"
                    }
                ]
            }
        ],
        "generationConfig": {
            "temperature": 0.2,
            "responseMimeType": "application/json"
        }
    }
    
    # Build URL with API key
    url = f"{GEMINI_URL}?key={GEMINI_API_KEY}"
    
    print(f"\n📍 Endpoint: {GEMINI_URL}")
    print(f"🔑 API Key: {GEMINI_API_KEY[:20]}...{GEMINI_API_KEY[-10:]}")
    print(f"\n🚀 Sending request...")
    
    try:
        response = requests.post(
            url,
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=30
        )
        
        print(f"\n✅ HTTP Status: {response.status_code}")
        
        if response.status_code == 200:
            print("✅ SUCCESS! API is working!")
            data = response.json()
            print(f"\n📦 Response:")
            print(json.dumps(data, indent=2))
            
            # Try to extract the text
            if "candidates" in data and len(data["candidates"]) > 0:
                text = data["candidates"][0]["content"]["parts"][0]["text"]
                print(f"\n💬 AI Response: {text}")
            
            return True
        else:
            print(f"❌ Error {response.status_code}")
            print(f"\n📋 Response:")
            try:
                print(json.dumps(response.json(), indent=2))
            except:
                print(response.text)
            return False
            
    except requests.exceptions.Timeout:
        print("❌ ERROR: Request timeout (30 seconds)")
        return False
    except requests.exceptions.ConnectionError as e:
        print(f"❌ ERROR: Connection failed")
        print(f"   {e}")
        return False
    except Exception as e:
        print(f"❌ ERROR: {e}")
        return False

def test_with_image():
    """Test with actual image if CAPY.png exists."""
    print("\n" + "=" * 60)
    print("Testing with Image")
    print("=" * 60)
    
    img_path = Path("app/src/main/res/mipmap-mdpi/ic_launcher.png")
    
    if not img_path.exists():
        print(f"⚠️  Image not found: {img_path}")
        return False
    
    print(f"\n📸 Encoding image: {img_path}")
    
    try:
        with open(img_path, "rb") as f:
            img_data = f.read()
            base64_img = base64.b64encode(img_data).decode('utf-8')
        
        print(f"✅ Image encoded ({len(base64_img)} chars)")
        
        # Vision request
        payload = {
            "contents": [
                {
                    "parts": [
                        {"text": "What is this image? Describe it briefly."},
                        {
                            "inline_data": {
                                "mime_type": "image/png",
                                "data": base64_img
                            }
                        }
                    ]
                }
            ],
            "generationConfig": {
                "temperature": 0.2
            }
        }
        
        url = f"{GEMINI_URL}?key={GEMINI_API_KEY}"
        
        print(f"\n🚀 Sending vision request...")
        response = requests.post(
            url,
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=60
        )
        
        print(f"\n✅ HTTP Status: {response.status_code}")
        
        if response.status_code == 200:
            print("✅ Vision API is working!")
            data = response.json()
            
            if "candidates" in data and len(data["candidates"]) > 0:
                text = data["candidates"][0]["content"]["parts"][0]["text"]
                print(f"\n💬 AI Response: {text}")
            
            return True
        else:
            print(f"❌ Error {response.status_code}")
            try:
                print(json.dumps(response.json(), indent=2))
            except:
                print(response.text)
            return False
            
    except Exception as e:
        print(f"❌ ERROR: {e}")
        return False

if __name__ == "__main__":
    print("\n🔍 Diagnostic Test for Gemini API\n")
    
    # Test 1: Basic text
    success1 = test_api()
    
    # Test 2: With image
    if success1:
        success2 = test_with_image()
    
    print("\n" + "=" * 60)
    print("SUMMARY")
    print("=" * 60)
    
    if success1:
        print("✅ Text API: WORKING")
        print("🎉 Your API key is valid and the endpoint is correct!")
        print("\n📱 If the Android app still fails, the issue is in the app code.")
    else:
        print("❌ Text API: FAILED")
        print("⚠️  Check:")
        print("   1. API key is correct (no spaces/typos)")
        print("   2. Internet connection is working")
        print("   3. Gemini API is accessible in your region")
    
    print()
