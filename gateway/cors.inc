# Snippet de CORS incluido en cada location del gateway. Debe vivir DENTRO
# de un location porque el "if" que gestiona el preflight OPTIONS solo es un
# contexto valido para add_header dentro de location ("if in location").
add_header Access-Control-Allow-Origin $cors_origin always;
add_header Access-Control-Allow-Credentials "true" always;
add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
add_header Access-Control-Allow-Headers "Authorization, Content-Type" always;

if ($request_method = OPTIONS) {
    add_header Access-Control-Allow-Origin $cors_origin always;
    add_header Access-Control-Allow-Credentials "true" always;
    add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
    add_header Access-Control-Allow-Headers "Authorization, Content-Type" always;
    add_header Content-Length 0;
    add_header Content-Type text/plain;
    return 204;
}
