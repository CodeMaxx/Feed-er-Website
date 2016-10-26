from django.shortcuts import render,reverse, get_object_or_404
from django.http import HttpResponse, HttpResponseRedirect
from django.contrib.auth.decorators import login_required
from django.contrib.auth.models import User
from django.contrib.auth import logout, authenticate, login
from .models import *
# Create your views here.

## Show the main view
## More like index page
def signin(request):
    if request.method == "GET":
        user = request.user
        if user.is_authenticated():
            return HttpResponseRedirect('home')

        return render(request, "signin.html")


    elif request.method == "POST":

        username = request.POST['username']
        password = request.POST['password']

        user = authenticate(username=username, password=password)
        if user is None:
            return HttpResponse("User not found.")
        login(request, user)
        return HttpResponseRedirect('home')


## Signup view which takes data and creates the account
## View is here
def signup(request):

    if request.method == "GET":
        return HttpResponseRedirect(reverse('website:signin'))
    elif request.method == "POST":
        try:
            fullname = request.POST['fullname']
            username = request.POST['sUsername']
            password = request.POST['sPassword']
            isProf = request.POST.get('prof')
        except:
            return HttpResponse(request)

        ## We have the data
        userexists = User.objects.filter(username=username)
        if len(userexists) != 0:
            #return HttpResponseRedirect(reverse('website:signin'))
            return HttpResponse("User already exists. Please login.")

        newuser = User.objects.create(username=username,email=username)
        newuser.set_password(password)
        newuser.save()

        if isProf == "on":
            mtype = "PR"
        else:
            mtype = "TA"

        newmember = Member.objects.create(
            user=newuser,
            fullname=fullname,
            mtype=mtype,
        )
        newmember.save()
        return HttpResponseRedirect(reverse('website:signin'))



## Add views for Home Page and logic 
## Will contain different stuff for Admins and TAs/Profs
def home(request):
    ## Anon users are shooed away
    if request.user.is_anonymous():
        return HttpResponseRedirect(reverse('website:signin'))

    ## get member and log out if student
    user = User.objects.get(username=request.user.username)
    member = Member.objects.get(user=user)

    context = {
        'member':member,
    }

    if member.mtype == "ST":
        logout(request)
        return HttpResponse('You are not allowed to visit this page. Please use the app since you are a student.')

    return render(request, 'home.html', context)

## Signout and return to homepage
def signout(request):
    logout(request)
    return HttpResponseRedirect(reverse('website:signin'))

## Complete Registration
## Completes facebook signup
def completeReg(request):

    if request.method == "GET":    # the user is registered now
        user = request.user

        if not user.is_authenticated():
            return HttpResponse('Invalid Facebook redirect!')

        context = {
            'fullname':user.first_name + ' ' + user.last_name,
            'username':user.username,
        }
        return render(request,'complete-reg.html',context)

    elif request.method == "POST":

        try:
            fullname = request.POST['fullname']
            username = request.POST['username']
            isProf = request.POST.get('prof')
        except:
            return HttpResponse("Invalid fields!")

        user = User.objects.get(username=username)

        mtype = "PR" if isProf else "TA"

        member = Member.objects.filter(user=user)
        if len(member) != 0:
            return HttpResponse('This user already exists! Try logging in!')

        member = Member.objects.create(user=user,fullname=fullname,mtype=mtype)
        member.save()
        return HttpResponseRedirect(reverse('website:home'))

    else:
        return HttpResponse('Invalid request sent!')


### View courses, all courses for admins, Selected courses for Profs, or TAs
@login_required
def view_courses(request):

    member = Member.objects.get(user=request.user)

    if member.mtype == "ST":
        return HttpResponse("You cannot access the site. Please use the app.")

    ## Admin, bring up all the courses
    elif member.mtype == "AD":
        all_courses = Course.objects.all()
        print (len(all_courses))
        
        if(len(all_courses) == 0):
        	return render(request, 'view_courses.html', {'error':'No courses to view. Add some courses first.'})
        try:
        	profs = [course.members.filter(mtype="PR")[0] for course in all_courses]
        except:
        	return render(request, 'view_courses.html', {'error':'No courses to view. Add some courses first.'})


        return render(request, 'view_courses.html', {'all_courses': all_courses, 'member':member, 'profs':profs})
    ## Prof or TA, bring up the courses of the prof or TA
    else:
        all_courses = member.course_set.all()
        return render(request, 'view_courses.html', {'all_courses': all_courses, 'member':member})


### Add courses for Admins
@login_required
def add_courses(request):
    member = Member.objects.get(user=request.user)
    if request.method == "GET":
        print(member.mtype)
        if member.mtype == "ST":
            return HttpResponse("You cannot access the site. Please use the app.")

        profs = Member.objects.filter(mtype="PR")
        students = Member.objects.filter(mtype="ST")

        context = {
            'mtype':member.mtype,
            'profs':profs,
            'students':students,
        }

        return render(request,'add_courses.html',context)

    elif request.method == "POST":
        try:
            course_name = request.POST['name']
            code = request.POST['code']
            semester = int(request.POST['semester'])
        except:
            return HttpResponse("Invalid POST data.")

        course = Course.objects.create(
            name=course_name,
            course_code=code,
            semester=semester
        )

        prof = request.POST.get('prof')
        students = request.POST.getlist('student')
        print(prof,students)

        prof = Member.objects.get(user__username=prof)
        course.members.add(prof)

        for student in students:
            stuObj = Member.objects.get(user__username=student)
            course.members.add(stuObj)

        course.save()
        return HttpResponseRedirect(reverse("website:home"))

@login_required
def course_detail(request,pk):
    
    member = Member.objects.get(user=request.user)
    course = get_object_or_404(Course,pk=pk)
    
    if member.mtype == "ST":
        return HttpResponse('You cannot access the site. Please use the app.')

    elif member.mtype == "AD":
        feedbacks = course.feedback_set.all()
        assignments = course.assignment_set.all()
        courseprof = course.members.filter(mtype="PR")[0]
        
    else:
        if course not in member.course_set.all():
            return render(request, 'course_detail.html',{'error':"Sorry, this is not your course, you aren't allowed to see it."})

        feedbacks = course.feedback_set.all()
        assignments = course.assignment_set.all()
        courseprof = None


    context = {
        'feedbacks':feedbacks,
        'member':member,
        'course':course,
        'assignments':assignments,
        'courseprof':courseprof
    }
        
    return render(request, 'course_detail.html',context)


#### Assignment request

@login_required()
def assigns(request):
    member = Member.objects.filter(user=request.user)
    all_courses = Course.objects.filter(members=member)
    assignments = []
    single_course_assign = []
    for course in all_courses:
        assign_for_course = course.objects.filter(course=course)
        for assign in assign_for_course:
            single_course_assign.append(assign)
        assignments.append(single_course_assign)
        single_course_assign = []

    context = { 'courses': all_courses,
                'assigns': assignments}
    return render(request, 'assigns.html', context)


@login_required()
def add_assigns(request):
    if request.method == "GET":
        return render(request, 'add_assigns.html')
    return HttpResponse('Adding assignment')

