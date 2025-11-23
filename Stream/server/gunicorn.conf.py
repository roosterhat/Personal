bind = "0.0.0.0:3001"
wsgi_app = "server:appStart()"

def worker_abort(worker):
    print("worker_abort")
    worker.app.wsgi().cleanup()
