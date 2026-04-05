# Postman

## Get Postman import from below

<https://api.postman.com/collections/39000944-5ef7bb1c-4454-4b42-8636-60c79ea862fb?access_key=PMAT-01KGNHWSF0AXTC40148B71R0X4>

## How to import to Postman

Select Import --> Paste the content in the above link to the text box --> Done.

## Set up

In the Variables tab of DATN, set variable like IdentityURL

Login credential:

```json
{
    "email": "admin@gmail.com",
    "password": "admin"
},
{
    "email": "buyer@gmail.com",
    "password": "buyer"
}
```

After login, you need to copy the response token. To use it in other request, change to the Authorization tab of that request, choose Bearer token and paste the token.
