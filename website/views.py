from django.shortcuts import render,reverse
from django.http import HttpResponse, HttpResponseRedirect
from django.contrib.auth.decorators import login_required
from django.contrib.auth.models import User
from django.contrib.auth import logout 
from .models import *
# Create your views here.

## Show the main view
def signin(request):
	if request.method == "GET":
		user = request.user
		if user.is_authenticated():
			return HttpResponseRedirect('home')

		return render(request, "signin.html")



## Signup view which takes data and creates the account
def signup(request):

	if request.method != "POST":
		return HttpResponse('Invalid Response')
	else:
		try:
			fullname = request.POST['fullname']
			username = request.POST['sUsername']
			password = request.POST['sPassword']
			isProf = request.POST['prof']
		except:
			return HttpResponseRedirect('signin')

		## We have the data
		userexists = User.objects.filter(username=username)
		if len(userexists) != 0:
			return HttpResponseRedirect('signin')

		newuser = User.objects.create(username=username, password=password)
		newuser.save()
		mtype = "PR" if isProf==True else "TA"
		
		newmember = Member.objects.create(
			user=newuser,
			fullname = fullname,
			mtype = mtype,
		)
		newmember.save()

		return HttpResponseRedirect('signin')



@login_required
def home(request):
	## Anon users are shooed away
	try:
		username = request.user.username
	except:
		return HttpResponseRedirect('signin')

	## get member and log out if student
	member = User.objects.get(username=request.user.username)
	try:
		member = Member.objects.get(user=member)
	except:
		return HttpResponse("You are not allowed to visit this page.")

	if member.mtype == "ST":
		logout(request)
		return HttpResponse('You are not allowed to visit this page. Please use the app since you are a student.')

	return HttpResponse("Home page!")




def logout(request):
	return HttpResponse('Sign in page!')

def courses(request):
	return HttpResponse('Sign in page!')

def add_courses(request):
	return HttpResponse('Sign in page!')

def assigns(request):
	return HttpResponse('Sign in page!')

def add_assigns(request):
	return HttpResponse('Sign in page!')

def course_detail(request,pk,year,sem):
	return HttpResponse('Sign in page!')
