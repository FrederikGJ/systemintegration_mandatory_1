#!/usr/bin/env bash
# Kalder alle 15 RPC'er i gRPC-API'et med grpcurl, ligesom en Postman-collection.
# Serveren skal køre først, se README.md.
#
#   ./grpc.sh [books|authors|companies|errors|all]   (default: all)
#   GRPC_HOST=localhost:9091 ./grpc.sh               (anden server)
set -euo pipefail

HOST="${GRPC_HOST:-localhost:9090}"
MAX_LINES=20

# go install lægger grpcurl i ~/go/bin, som ikke altid er på PATH
command -v grpcurl > /dev/null || PATH="$PATH:$HOME/go/bin"
command -v grpcurl > /dev/null || {
    echo "grpcurl mangler: go install github.com/fullstorydev/grpcurl/cmd/grpcurl@latest" >&2
    exit 1
}
grpcurl -plaintext -connect-timeout 3 "$HOST" list > /dev/null || {
    echo "Ingen gRPC-server på $HOST. Start den først, se README.md" >&2
    exit 1
}

# Viser et svar, men højst MAX_LINES linjer, så lister ikke fylder hele terminalen
show() {
    local lines
    lines=$(wc -l <<< "$1")
    head -n "$MAX_LINES" <<< "$1"
    if (( lines > MAX_LINES )); then
        echo "... (forkortet, $lines linjer i alt)"
    fi
}

# Kalder en RPC og viser request og svar. Svaret gemmes i RESPONSE. Stopper scriptet ved fejl
call() {
    echo "### $1"
    echo "$2"
    RESPONSE=$(grpcurl -plaintext -d "$2" "$HOST" "library.$1")
    show "$RESPONSE"
    echo
}

# Kalder en RPC, der skal fejle med en bestemt statuskode (fx NotFound)
expect_error() {
    echo "### $1 (forventer $3)"
    echo "$2"
    local out
    if out=$(grpcurl -plaintext -d "$2" "$HOST" "library.$1" 2>&1); then
        echo "Uventet: kaldet lykkedes" >&2
        exit 1
    fi
    echo "$out"
    grep -q "Code: $3" <<< "$out" || {
        echo "Uventet statuskode" >&2
        exit 1
    }
    echo
}

# id'et fra det seneste svar, fx en bog, Create netop har oprettet
created_id() {
    sed -n 's/^  "id": \([0-9]*\).*/\1/p' <<< "$RESPONSE"
}

books() {
    call BookService/ListBooks '{}'
    call BookService/ListBooks '{"author_id": 394}'
    call BookService/GetBook '{"id": 1000}'
    call BookService/CreateBook '{"book": {"title": "Testbog", "author_id": 394, "publishing_company_id": 74}}'
    local id
    id=$(created_id)
    call BookService/UpdateBook '{"book": {"id": '"$id"', "title": "Rettet titel", "author_id": 394, "publishing_year": 2024, "publishing_company_id": 74}}'
    call BookService/DeleteBook '{"id": '"$id"'}'
}

authors() {
    call AuthorService/ListAuthors '{}'
    call AuthorService/GetAuthor '{"id": 394}'
    call AuthorService/CreateAuthor '{"author": {"name": "Platon"}}'
    local id
    id=$(created_id)
    call AuthorService/UpdateAuthor '{"author": {"id": '"$id"', "name": "Platon", "surname": "Aristokles"}}'
    call AuthorService/DeleteAuthor '{"id": '"$id"'}'
}

companies() {
    call PublishingCompanyService/ListPublishingCompanies '{}'
    call PublishingCompanyService/GetPublishingCompany '{"id": 74}'
    call PublishingCompanyService/CreatePublishingCompany '{"publishing_company": {"name": "Testforlag"}}'
    local id
    id=$(created_id)
    call PublishingCompanyService/UpdatePublishingCompany '{"publishing_company": {"id": '"$id"', "name": "Rettet forlag"}}'
    call PublishingCompanyService/DeletePublishingCompany '{"id": '"$id"'}'
}

errors() {
    expect_error BookService/GetBook '{"id": -1}' NotFound
    expect_error BookService/CreateBook '{"book": {"title": "  ", "author_id": 394, "publishing_company_id": 74}}' InvalidArgument
    expect_error AuthorService/UpdateAuthor '{"author": {"id": -1, "name": "Findes ikke"}}' NotFound
    expect_error PublishingCompanyService/DeletePublishingCompany '{"id": -1}' NotFound
}

case "${1:-all}" in
    books)     books ;;
    authors)   authors ;;
    companies) companies ;;
    errors)    errors ;;
    all)       books; authors; companies; errors ;;
    *)
        echo "Brug: $0 [books|authors|companies|errors|all]" >&2
        exit 1
        ;;
esac
