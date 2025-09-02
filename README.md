# ChainLog App
App para a plataforma Android que visa coletar informações úteis para perícia computacional.

## Configuração
Necessário baixar arquivo **google-services.json** no Firebase e adicioná-lo na raiz do diretório /app.

## Extra
Para ver logs do Firebase Analytics em tempo real no DebugView:
Habilitar: `adb shell setprop debug.firebase.analytics.app com.example.chainlog`
Desabilitar: `adb shell setprop debug.firebase.analytics.app .none.`