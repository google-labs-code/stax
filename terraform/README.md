# Terraform

Terraform is used to bootstrap and manage the GCP resources for the Stax web app.

## File Structure

```
terraform/
   modules/ # Contains reusable modules for GCP resources
      gcp_pubsub/ # Module for Google Cloud Pub/Sub
      gcp_run/ # Module for Google Cloud Run
      gcp_sql/ # Module for Google Cloud SQL
   quickstart/ # Contains the example deployment main.tf file and other configuration files
```

## Prerequisites
Create a GCP project and enable billing. Note down the project id (NOT the project number) on the welcome page in. You will need this for $PROJECT_ID in later steps.

```shell
export PROJECT_ID=<your project id>
```

Replace the project id in `terraform/quickstart/main.tf` with the one you just created.

## Bootstrap your GCP project with Terraform

1. Download Terraform. You can either download from https://developer.hashicorp.com/terraform/install or use Homebrew:

```bash
brew install terraform
```

2. Install the Google Cloud CLI ([guide](https://docs.cloud.google.com/sdk/docs/install)).
   To initialize the gcloud CLI, run the following command:

```bash
gcloud init
```

Note: If you've installed the gcloud CLI previously, make sure you have the latest
version by running `gcloud components update`.

To set the default project for your Cloud Run service:

```bash
gcloud config set project $PROJECT_ID
```

3. Run the following command. Make sure billing is enabled for this Google Cloud project before running the command.

```bash
cd $REPO/terraform/quickstart
gcloud auth application-default login
terraform init -upgrade
terraform plan
terraform apply
```

This step brings up the necessary GCP resources to run the services in Cloud Run. You are now ready to build and deploy the services in Cloud Run.

## Build and push container images
You can use GCP Cloud Run CLI to build and push the container images for UI server and backend server to GCP Artifact Registry, then deploy the images in one step.

```shell
export PROJECT_ID=<your project id>
export REGION=us-central1

# From the backend java directory using Dockerfile.
gcloud run deploy stax-ui --project=${PROJECT_ID} --source . --region=${REGION}

# From the UI directory. Make sure you have set all the NEXT_PUBLIC_* environment variables. We use the `gcloud run deploy` to build and push the container image.
gcloud run deploy stax-backend --project=${PROJECT_ID} --source . --region=${REGION}
```

After these steps, the images will be present in the following locations.
- `${local.region}-docker.pkg.dev/${local.project_id}/cloud-run-source-deploy/stax-ui`
- `${local.region}-docker.pkg.dev/${local.project_id}/cloud-run-source-deploy/stax-backend`


## Use Google oauth for application logins
The default authentication method for Stax is Google SSO. We suggest removing the authorization checks for the application for local runs. 

If you decide Google oauth is needed, there are a few GCP resources to be updated. 
1) Configure a Google oauth client ([guide](https://developers.google.com/identity/protocols/oauth2/web-server))
2) Update the environment variables `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` for the backend server. 

   You can do this by uncommenting the `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` sections in `quickstart/cloud_run_backend.tf`. 
Then, go to Secret Manager in your GCP project (GCP web console) to store the client secret string. Use secret id `google_oauth_client_secret`. Lastly, uncomment the `google_oauth_client_secret` section in `terraform/quickstart/secrets.tf`. Run terraform to apply the changes

   ```bash
   terraform plan
   terraform apply
   ```
  