kubectl config use-context docker-desktop

kubectl run NAME --image=image [--env="key=value"] [--port=port] [--dry-run=server|client] [--overrides=inline-json] [--command] -- [COMMAND] [args...]



kubectl run foo --image=stream-worker2 --image-pull-policy='Never' -- ace of spades