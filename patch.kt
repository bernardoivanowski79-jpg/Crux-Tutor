// GOOGLE SSO TAB
Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    if (isAuthenticatingGoogle) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(modifier = Modifier.size(36.dp), color = Color(0xFF4285F4))
            Spacer(modifier = Modifier.height(14.dp))
            Text("Conectando à sua Conta do Google...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    } else if (isAlreadySignedIn) {
