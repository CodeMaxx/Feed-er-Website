.PHONY: help run mig mkmg shell

help:
	@echo
	@echo "make run : runserver"
	@echo "make mig: migrate"
	@echo "make mkmg: makemigrations"
	@echo "feel free to add new features and update the help section."
	@echo
run:
	@python3 manage.py runserver

mig:
	@python3 manage.py migrate

mkmg:
	@python3 manage.py makemigrations

shell:
	@python3 manage.py shell

