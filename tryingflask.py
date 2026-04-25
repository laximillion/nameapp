from flask import Flask, redirect, url_for, request, render_template, flash
import os

app = Flask(__name__)
app.secret_key = "secret-key-change-this"

basedir = os.path.abspath(os.path.dirname(__file__))

majors_file = os.path.join(basedir, 'static/txt/all_majors.txt')
minors_file = os.path.join(basedir, 'static/txt/all_minors.txt')


def load_list(file_path):
    items = []
    with open(file_path, "r", encoding="utf-8") as file:
        for line in file:
            clean = line.strip()
            if clean:
                items.append(clean)
    return items


majorslist = load_list(majors_file)
minorslist = load_list(minors_file)


# map dropdown text -> htmltablecs.html section id base
PROGRAM_NAME_MAP = {
    # majors + minors
    "Film Media Theater (FMT)": "FMT",
    "Gender Studies": "Gender",
    "Geography": "Geo",
    "History": "Hist",
    "International Relations": "International",
    "Middle Eastern Studies": "Middle",
    "Neuroscience and Behavior": "Neuro",
    "Psychology": "Psych",
    "Psychology and Education: Early Childhood or Elementary Education": "PsychEdTeach",
    "Psychology and Education: Not Leading to Teacher Licensure": "PsychEdOther",
    "Romance Languages and Cultures": "Romance",
    "South Asian Studies": "SAS",
    "Statistics": "Stat",
    "Africana Studies": "Africana",
    "Anthropology": "Anthropology",
    "Architectural Studies": "Arch",
    "Art History": "ArtHist",
    "Art Studio": "ArtStudio",
    "Astronomy": "Astro",
    "Biochemistry": "BioChem",
    "Biological Sciences": "Bio",
    "Chemistry": "Chem",
    "Classical Studies": "ClassicsStudies",
    "Computer Science": "CompSci",
    "Data Science": "DataSci",
    "Dance": "Dance",
    "East Asian Studies": "EastAsian",
    "Environmental Studies": "Envir",
    "Mathematics": "Math",
    "Music": "Music",
    "Philosophy": "Philosophy",
    "Physics": "Physics",
    "Politics": "Politics",
    "Religion": "Religion",
    "Sociology": "Sociology",
    "Spanish": "Spanish",
    "Economics": "Econ",
    "English": "English",
    "Environmental Studies": "EnvironmentalStudies",
    "French": "French",
    "Gender Studies": "GenderStudies",
    "Geology": "Geology",
    "Italian": "Italian",

    # minors only / special
    "Arabic": "Arabic",
    "Asian Studies": "Asian",
    "Chinese": "Chinese",
    "Educational Studies": "EducationalStudies",
    "Entrepreneurship, Organizations, and Society": "EntrepreneurshipOrganizationsSociety",
    "Film Media Theater (FMT)": "FMT",
    "Japanese": "Japanese",
    "Jewish Studies": "JewishStudies",
    "Latin American Studies": "LatinAmericanStudies",

    "Economics_minor": "Econ",
    "Educational Studies_minor": "",
    "Entrepreneurship, Organizations, and Society _minor": "",
    "Environmental Studies_minor": "",
    "Gender Studies_minor": "",
    "Geography_minor": "",
    "History_minor": "",
    "Jewish Studies_minor": "",
    "Latin American Studies_minor": "",
    "Psychology_minor": "",
    "Psychology and Education: Early Childhood and Elementary Teaching License_minor": "",
    "Psychology and Education: Teaching Licenses in Middle or Secondary Education, Foreign Language, Dance, Music, Theater, or Visual Art_minor": "",
    "Romance Languages and Cultures _minor": "",
    "Statistics_minor": "",

    # special CRPE names
    "Critical Race and Political Economy (CRPE): CRPE Pathway": "CRPEOne",
    "Critical Race and Political Economy (CRPE): Critical Social Thought Pathway": "CRPE-CST",
    "Critical Race and Political Economy (CRPE): Africana Studies Pathway": "CRPE-Africana",
    "Critical Race and Political Economy (CRPE): Latinx Pathway": "CRPE-Latinx",
}


def to_section_name(program_name, program_type):
    """
    Converts dropdown text like 'Computer Science'
    into html section id like 'CompSci' or 'CompSci Minor'
    """
    if program_type == "Major":
        base = PROGRAM_NAME_MAP.get(program_name)
    else:
        base = PROGRAM_NAME_MAP.get(program_name+"_minor")

    print(program_name)
    if not base:
        return None

    return f"{base} {program_type}"


@app.route('/')
def home():
    return render_template(
        'main.html',
        majorslist=majorslist,
        minorslist=minorslist
    )


@app.route('/requirements', methods=['POST'])
def requirements():
    major1 = request.form.get('major1', '').strip()
    major2 = request.form.get('major2', '').strip()
    minor = request.form.get('minor', '').strip()

    major_count = sum(bool(x) for x in [major1, major2])
    minor_count = 1 if minor else 0

    if not major1:
        flash("Please choose your first major.")
        return redirect(url_for('home'))

    # only allowed:
    # 1 major
    # 2 majors
    # 1 major + 1 minor
    if major_count == 2 and minor_count == 1:
        flash(
            "Choose only one of these: one major, two majors, or one major and one minor.")
        return redirect(url_for('home'))

    selected_items = []
    selected_names = []

    if major1:
        section = to_section_name(major1, "Major")
        selected_items.append({
            "name": major1,
            "type": "Major"
        })
        if section:
            selected_names.append(section)

    if major2:
        section = to_section_name(major2, "Major")
        selected_items.append({
            "name": major2,
            "type": "Major"
        })
        if section:
            selected_names.append(section)

    if minor:
        section = to_section_name(minor, "Minor")
        selected_items.append({
            "name": minor,
            "type": "Minor"
        })
        if section:
            selected_names.append(section)

    return render_template(
        'htmltablecs.html',
        selected_items=selected_items,
        selected_names=selected_names
    )


if __name__ == '__main__':
    app.run(debug=True)
