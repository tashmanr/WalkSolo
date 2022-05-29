//const googleAuth = require('google-auth-library');
//const SCOPES = ['https://www.googleapis.com/auth/cloud-platform'];
//
//async function getAccessToken() {
//    const serviceAccount = require('/Users/rebecca/StudioProjects/WalkSolo/app/google-services.json');
//    const jwtClient = new googleAuth.JWT(
//        serviceAccount.client_email,
//        null,
//        serviceAccount.private_key,
//        SCOPES,
//        null
//    );
//    return jwtClient.authorize().then((tokens) => tokens.access_token);
//}

const fetch = require('node-fetch');
const GCIP_API_BASE = 'https://identitytoolkit.googleapis.com/v2';

async function addIdpConfig(projectId, accessToken, idpId, clientId, clientSecret) {
    const uri = `${GCIP_API_BASE}/projects/${projectId}/defaultSupportedIdpConfigs?idpId=${idpId}`;
    const options = {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${accessToken}`
        },
        body: JSON.stringify({
            name: `projects/${projectId}/defaultSupportedIdpConfigs/${idpId}`,
            enabled: true,
            clientId: clientId,
            clientSecret: clientSecret,
        }),
    };
    return fetch(uri, options).then((response) => {
        if (response.ok) {
            return response.json();
        } else if (response.status == 409) {
            throw new Error('IdP configuration already exists. Update it instead.');
        } else {
            throw new Error('Server error.');
        }
    });
}

(async () => {
    const projectId = 'walksolo';
    const accessToken = await getAccessToken();
    const idpId = 'google.com';
    const clientId = '404644799177-lcak95imo7tcuiecr8lu4iag6ojshp4v.apps.googleusercontent.com';
    const clientSecret = 'your-google-client-secret';
    try {
        await addIdpConfig(projectId, accessToken, idpId, clientId, clientSecret);
    } catch (err) {
        console.error(err.message);
    }
})().catch(console.error);