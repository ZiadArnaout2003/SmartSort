# SmartSort: Intelligent Waste Classification


## Project Overview
**SmartSort** is an innovative Android application designed to revolutionize household waste sorting. Leveraging cutting-edge machine learning capabilities, SmartSort assists users in correctly categorizing recyclable items—specifically focusing on plastic bottles and cans—to promote efficient recycling and reduce contamination in waste streams.

The vision for SmartSort extends beyond just an app; it's a step towards smarter waste management. Imagine a future where a physical **Smart Bin** automatically sorts waste based on app-driven intelligence. This application serves as the core intelligence engine, capable of identifying common recyclable items.

---

## Core Functionality
- **Image-Based Classification:** Users can take a picture of a waste item or select one from their phone's gallery.
- **Machine Learning Integration:** A locally trained TensorFlow Lite model processes the image to identify the waste type.
- **Simplified Categories:**  
  - **Bottle/Can:** Common recyclables like plastic bottles, glass bottles, and metal cans.  
  - **Other Waste:** Anything else.
- **User Guidance:** Provides immediate feedback on whether an item is a bottle/can or something else, helping users make informed sorting decisions.

---

## Technology Stack
- **Mobile Platform:** Android (Java)  
- **Machine Learning Framework:** TensorFlow Lite  
- **Model Architecture:** Object Detection models (e.g., MobileNet-SSD or EfficientDet-Lite) optimized for on-device inference  
- **Dataset Source:** Publicly available image datasets of waste items  

---

## Why SmartSort?
- **Promote Accurate Recycling:** Reduces "wishcycling" and contamination, making recycling processes more effective.
- **Environmental Impact:** Contributes to less landfill waste and better resource recovery.
- **User-Friendly:** Simplifies the often-confusing task of waste sorting for everyday users.
- **Scalable Future:** Lays the groundwork for more advanced features, including integration with smart bins and wider waste category recognition.

---

## Getting Started
This documentation will guide you through:
1. Setting up your development environment.
2. Training the machine learning model.
3. Building the Android application.

---

## Demo Video
https://youtu.be/yw-W8tdukvc

