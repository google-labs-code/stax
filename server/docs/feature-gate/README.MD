# **Feature Gate System**

This document explains how to use the dynamic feature gate system to toggle application functionality without a new deployment.

## **How It Works**

1. **Source of Truth:** Flag configurations are stored in JSON files in a dedicated Git repository.
2. **Deployment:** A commit to the feature-flags branch triggers a CI/CD pipeline that uploads the JSON file to Google Cloud Storage (GCS).
3. **Notification:** GCS sends an event to Google Pub/Sub.
4. **Redis Update:** The application receives this event, downloads the file from GCS, and updates the central Redis cache.
5. **Local Cache:** Each application instance synchronizes its in-memory cache from Redis periodically (every 60 seconds) or when forced by an event.

## **1\. Managing Feature Flags**

Flags are managed via commits to the planck-feature-configs repository.

### **File Structure**

The configuration file must follow the pattern: {app-name}-features-{app-env}.json.

* app-name: The application name (e.g., stax).
* app-env: The environment name (dev, prod, etc.).

**Example (stax-features-dev.json):**

{  
"version": "1.0.2",  
"metadata": {  
"commit\_hash": "a1b2c3d4",  
"author": "dev@example.com",  
"deploy\_time\_utc": "2025-10-07T12:00:00Z"  
},  
"features": \[  
{  
"key": "stax-enable-new-dashboard",  
"description": "Enables the new Vue.js dashboard.",  
"enabled": true  
},  
{  
"key": "stax-use-optimized-search",  
"description": "Switches to the new search algorithm.",  
"enabled": false  
}  
\]  
}

### **Toggling a Feature**

To change a feature's state, edit the enabled field (true or false) and commit the change to the feature-flags branch. The CI/CD pipeline will automatically deploy the update.

## **2\. Application Configuration (application.yml)**

The system is configured via the feature-gate block in your application.yml file.

feature-gate:  
app-name: stax  
app-env: ${APP\_ENV}  
use-local-cache: true

* app-name: The name of the application. Used to construct Redis keys and filenames (e.g., stax).
* app-env: The deployment environment. Used to construct unique Redis keys, filenames, and Pub/Sub subscription names. It is dynamically set via the APP\_ENV environment variable.
* use-local-cache: Determines the strategy for checking features.
   * true (default): **Cached Mode.** The application maintains a high-performance in-memory cache. Feature checks are instant. The cache is synchronized from Redis every 60 seconds and immediately upon receiving a Pub/Sub update event. **This is the recommended mode for production.**
   * false: **Direct Redis Mode.** The application checks the feature flag value directly from Redis for every request. If Redis is unavailable, it falls back to the values in the default-feature-flags.json file. This mode provides real-time consistency but adds a small network latency to each check.

## **3\. Using Feature Flags in Code**

There are two ways to use flags: declaratively (with an annotation) and programmatically.

### **A. Declarative (for REST Controllers)**

This is the easiest way to enable or disable an entire endpoint. Use the @FeatureEnabled("your-feature-key") annotation on a whole controller or a controller method.

**Example:**

@RestController  
@RequestMapping("/api/dashboard")  
public class DashboardController {

    @GetMapping("/new")  
    @FeatureEnabled("stax-enable-new-dashboard")  
    public ResponseEntity\<DashboardData\> getNewDashboard() {  
        // This code only runs if the 'stax-enable-new-dashboard' flag is enabled.  
        return ResponseEntity.ok(dashboardService.getNewData());  
    }  
}

* **If enabled (true):** The method executes normally, returning 200 OK.
* **If disabled (false):** The method is not executed, and the client receives a 404 Not Found response.

### **B. Programmatic (for any logic)**

To control a specific part of your logic, inject the FeatureGateService.

**Example:**

@Service  
public class SearchService {

    private final FeatureGateService featureGateService;

    @Autowired  
    public SearchService(FeatureGateService featureGateService) {  
        this.featureGateService \= featureGateService;  
    }

    public SearchResults findResults(String query) {  
        if (featureGateService.isFeatureEnabled("stax-use-optimized-search")) {  
            // Use the new, optimized algorithm  
            return performOptimizedSearch(query);  
        } else {  
            // Use the old, legacy algorithm  
            return performLegacySearch(query);  
        }  
    }  
}

## **4\. Local Development & Testing**

### **Environment Setup**

1. **Run local Redis:** Can be started as a container via podman.
      ```bash
   podman pull redis
   podman run --name redis-app -d -p 6379:6379 redis
      ```
2. Run Pub/Sub emulator:  
   gcloud beta emulators pubsub start \--host-port=localhost:8085
3. **Configure emulator:** In a new terminal, set the environment variables and create your test topic and subscription for your project (e.g., test-project).
4. **Run the application:** Start the application with the local profile active.

### **Testing Flag Updates**

To simulate a GCS file update, publish a message to your **local** Pub/Sub emulator.

1. **Publish the event to the emulator:**  
   \# Point terminal to the emulator  
   export PUBSUB\_EMULATOR\_HOST=localhost:8085

   \# Publish the message  
   gcloud pubsub topics publish gcs-feature-gate \\  
   \--project=test-project \\  
   \--message='{"bucket":"your-test-bucket","name":"stax-features-dev.json"}'

   *(Note: The application still needs valid GCS credentials to download this file. Ensure the file exists in the actual GCS bucket).*
   There is also an option to use the UI to publish the messages to the emulator. Please refer to pubsub-emulator-ui in github if needed
2. Check your application logs. The event should be received, and your local Redis instance should be updated.